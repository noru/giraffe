#!/usr/local/bin/python2.7

import os, sys, shutil, subprocess, ConfigParser, logging
import zipfile, httplib, time

import boto
from boto.s3.bucket import Bucket
from boto.s3.key import Key
from boto import utils, ec2

import smtplib
from email.mime.text import MIMEText

config = ConfigParser.RawConfigParser()
config.read('deploy.cfg')

#TODO: These  settings should be passed into the instance via user data
AWS_ACCESS_KEY_ID = config.get('Aws', 'Access_Key_Id')
AWS_SECRET_ACCESS_KEY = config.get('Aws', 'Secret_Access_Key')

ENVIRONMENT = config.get('Setting', 'Environment')
PORT = config.get('Setting', 'Port')

BUILD_S3_BUCKET = config.get('Build', 'S3_Bucket')
LOCAL_BUILD_KEY = config.get('Build', 'Local_Build_Key')

WORKING_FOLDER = config.get('Sync', 'Working_Dir')

EMAIL_HOST = config.get('Email', 'Host')
EMAIL_USERNAME = config.get('Email', 'Username')
EMAIL_PASSWORD = config.get('Email', 'Password')
EMAIL_FROM = config.get('Email', 'From')
EMAIL_TO = config.get('Email', 'To')

change_log = ''

def init_log(log_path, log_file_name):
    if not os.path.exists(log_path):
        os.mkdir(log_path)

    global LOGGER

    LOGGER = logging.getLogger('giraffe-auto-deployment-log')
    file_handler = logging.FileHandler(log_path + '/' + log_file_name)

    formatter = logging.Formatter('%(asctime)s %(message)s')
    file_handler.setFormatter(formatter)
    LOGGER.addHandler(file_handler)

    stream_handler = logging.StreamHandler(sys.stderr)
    LOGGER.addHandler(stream_handler)

    LOGGER.setLevel(logging.INFO)

    return LOGGER

def close_log():
    LOGGER.handlers = []

def send_email(subject, msg_body):
    logger.info('Sending notification email...')

    msg = MIMEText(msg_body)
    msg['Subject'] = subject
    msg['From'] = EMAIL_FROM
    msg['To'] = EMAIL_TO

    try:
        server = smtplib.SMTP()
        server.connect(EMAIL_HOST)
        server.starttls()
        server.login(EMAIL_USERNAME, EMAIL_PASSWORD)
        server.sendmail(EMAIL_FROM, EMAIL_TO, msg.as_string())
        server.quit()

        logger.info('Email sent out successfully')
        return True
    except Exception, e:
        logger.error("Failed to send out email notification, error: {}".format(str(e)))
        return False

#unzip installer package
def unzip_file(zip_file_name, unzip_to_dir = ''):
    logger.info('Unzipping file: ' + zip_file_name + ' to ' + unzip_to_dir)

    if not os.path.exists(unzip_to_dir):
        os.mkdir(unzip_to_dir, 0777)
    zfobj = zipfile.ZipFile(zip_file_name)

    for name in zfobj.namelist():
        name = name.replace('\\','/')

        filename = os.path.join(unzip_to_dir, name)

        if not os.path.exists(os.path.dirname(filename)):
            os.makedirs(os.path.dirname(filename))

        if not name.endswith('/'):
            ext_filename = os.path.join(unzip_to_dir, name)
            ext_dir= os.path.dirname(ext_filename)
            if not os.path.exists(ext_dir) : os.mkdir(ext_dir,0777)
            outfile = open(ext_filename, 'wb')
            outfile.write(zfobj.read(name))
            outfile.close()

def get_instance_id():
    instance_metadata = utils.get_instance_metadata(timeout = 0.5, num_retries = 1)
    return instance_metadata['instance-id']

def get_region():
    instance_metadata = utils.get_instance_metadata(timeout = 0.5, num_retries = 1)
    return instance_metadata['placement']['availability-zone'][:-1]

def get_instance_name():
    conn = ec2.connect_to_region(get_region(), aws_access_key_id = AWS_ACCESS_KEY_ID
        , aws_secret_access_key = AWS_SECRET_ACCESS_KEY)

    tags = conn.get_all_tags(filters = {'resource-id': get_instance_id(), 'key': 'Name'})
    if tags:
        return tags[0].value

    return 'Name-Not-Queriable'

def is_process_running(process_name):
    tmp = os.popen("ps -Af").read()
    if process_name not in tmp[:]:
        return False
    else:
        return True

# Get the latest local build number.  If not found, set to 0.0.0.0
def load_local_build_number():
    localBuild = "0.0.0.0"

    try:
        with open(WORKING_FOLDER + "/" + LOCAL_BUILD_KEY, "r") as f:
            localBuild = f.read()
    except:
        logger.info("No local build file exists, use default value")

    return localBuild

def update_local_build_number(build_number):
    try:
        with open(WORKING_FOLDER + "/" + LOCAL_BUILD_KEY, "w") as f:
            f.write(build_number)
        logger.info("Succeeded to update the local build number")
    except Exception as e:
        error_info = "Failed to update the local build number - {}".format(str(e))
        logger.error(error_info)
        raise Exception(error_info)

def sync_artifacts(localBuild):
    serverBuild = 'NotFound'

    try:
        s3_conn = boto.connect_s3(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
        bucket  = Bucket(s3_conn, BUILD_S3_BUCKET)

        # Get the server build.
        k = Key(bucket, "giraffe-" + ENVIRONMENT + "-build")
        serverBuild = k.get_contents_as_string()

        # If the local and server builds are not the same, sync the build.
        if localBuild != serverBuild:
            logger.info("Syncing to build #" + serverBuild + "...")

            # Get the build deployable from S3.
            logger.info("Downloading " + serverBuild + ".zip...")
            k = Key(bucket, "giraffe/giraffe-" + serverBuild + ".zip")
            k.get_contents_to_filename(WORKING_FOLDER + "/giraffe-" + serverBuild + ".zip")

            logger.info("Downloading " + serverBuild + ".txt...")
            k = Key(bucket, "giraffe/giraffe-" + serverBuild + ".txt")
            k.get_contents_to_filename(WORKING_FOLDER + "/giraffe-" + serverBuild + ".txt")

            global change_log
            try:
                with open(WORKING_FOLDER + "/giraffe-" + serverBuild + ".txt", "r") as f:
                    change_log = f.read()
            except:
                logger.warn("Failed to read change log")

            logger.info("... download completed")
    except Exception as e:
        logger.error("An error occurred in checking or downloading artifacts, error: {}".format(str(e)))
        raise Exception('Failed to check or download artifacts - {}'.format(str(e)))

    return serverBuild

def deploy(server_build_number):
    unzip_file(WORKING_FOLDER + '/giraffe-' + server_build_number + '.zip', WORKING_FOLDER + '/')

    logger.info("Kill the running java process...")
    os.system("ps -C java -o pid=|xargs kill")

    logger.info("Wait the process to quit...")
    counter = 0
    while (counter < 5):
        if not is_process_running('java'):
            break
        time.sleep(1)
        counter += 1

    if is_process_running('java'):
        logger.error("Java is still running, kill it forcely")
        os.system("ps -C java -o pid=|xargs kill -9")

    if is_process_running('java'):
        logger.error("Java can't be killed")
        raise Exception('Failed to stop the running instance')
		
    logger.info("Copy secret config file...")
    if (os.path.exists('secret.conf')):
        shutil.copyfile('secret.conf', WORKING_FOLDER + '/giraffe-' + server_build_number + '/conf/secret.conf')

    logger.info("Start giraffe with new version...")
    os.chdir(WORKING_FOLDER + '/giraffe-' + server_build_number + '/bin')
    os.system("chmod +x giraffe")
    os.system("nohup ./giraffe -Dhttp.port=" + PORT + " -Dconfig.resource=" + ENVIRONMENT + ".conf &")

    logger.info("Wait the process to be started...")
    counter = 0
    while (counter < 5):
        if is_process_running('java'):
            return
        time.sleep(1)
        counter += 1

    logger.error("Failed to restart giraffe")
    raise Exception('Failed to restart giraffe')

def resume_the_old_version(old_build_number):
    if is_process_running('java'):
        return True

    logger.info("Resume the old giraffe...")
    os.chdir(WORKING_FOLDER + '/giraffe-' + old_build_number + '/bin')
    os.system("chmod +x giraffe")
    os.system("nohup ./giraffe -Dhttp.port=" + PORT + " &")

    logger.info("Wait the process to be started...")
    counter = 0
    while (counter < 5):
        if is_process_running('java'):
            return True
        time.sleep(1)
        counter += 1

    error_info = "Failed to resume giraffe"
    logger.error(error_info)
    send_notification_email(False, old_build_number, '', error_info)

    return False

# If the error happens continuously, we only send out the first failure email
# to avoid the email flooding
def should_send_failure_email():
    try:
        with open(os.path.join(script_folder, 'update_result'), "r") as f:
            result = f.read()
            if result == "F":
                return False;
    except:
        logger.error("Failed to read the .update_result file")

    return True

def update_should_send_failure_email(succeeded):
    try:
        with open(os.path.join(script_folder, 'update_result'), "w") as f:
            if succeeded:
                f.write('S')
            else:
                f.write('F')
    except:
        logger.error("Failed to update the .update_result file")


def send_notification_email(succeeded, old_build_number, new_build_number, error_info):
    if not succeeded:
        if not should_send_failure_email():
            return;

    status = ''
    if succeeded:
        status = '[SUCCESS]'
    else:
        status = '[FAILED]'

    subject = status + '-[' + ENVIRONMENT.upper() +  ']: Deploy Giraffe'

    msg_body = 'New Version: ' + new_build_number + '\n'
    msg_body += 'Old Version: ' + old_build_number + '\n\n'
    msg_body += 'EC2 Instance Name: ' + get_instance_name() + '\n'
    msg_body += 'EC2 Instance Id: ' + get_instance_id() + '\n'
    msg_body += 'Region: ' + get_region() + '\n'
    if error_info != '':
        msg_body += '\nError: ' + error_info

    if change_log != '':
        msg_body += '\n\nChangeLog: ' + change_log

    send_email(subject, msg_body)

    update_should_send_failure_email(succeeded)

def main():
    global script_folder
    script_folder = os.path.split(os.path.realpath(sys.argv[0]))[0]

    global logger
    logger = init_log(script_folder, 'giraffe-deployment-history.log')

    logger.info("##Begin##")

    succeeded = False

    local_build_number = ''
    server_build_number = ''

    error_info = ''

    try:
        if not os.path.exists(WORKING_FOLDER):
            os.mkdir(WORKING_FOLDER)

        local_build_number = load_local_build_number()
        server_build_number = sync_artifacts(local_build_number)

        if local_build_number != server_build_number:
            if server_build_number != 'NotFound':
                deploy(server_build_number)
                succeeded = True

            if succeeded:
                logger.info("Local build #: " + local_build_number)
                logger.info("Server build #: " + server_build_number)

                update_local_build_number(server_build_number)
                send_notification_email(succeeded, local_build_number, server_build_number, '')
        else:
            logger.info("No new build")
            succeeded = True

    except Exception as e:
        succeeded = False
        error_info = str(e)
        logger.error("Auto-deployment failed with error: {}".format(error_info))

    if succeeded:
        logger.info("##End!##\n")
    else:
        send_notification_email(succeeded, local_build_number, server_build_number, error_info)
        resume_the_old_version(local_build_number)
        logger.error("##Failed!##\n")

if __name__ == '__main__':
    main()

