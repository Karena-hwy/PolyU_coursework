import socket 
import hashlib
import os
import ast
import getpass
import time
import sys
import msvcrt  # Windows
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes 
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend
    
#hash the password with random salt    
def hash_password_salt(salt, password):  
    if isinstance(salt, bytes): #convert salt to byte
        salt_bytes = salt
    elif isinstance(salt, str):
        if salt.startswith("b'") and salt.endswith("'"):
            try:
                salt_bytes = ast.literal_eval(salt)
            except (ValueError, SyntaxError):
                salt_bytes = salt.encode('utf-8')
        else:
            salt_bytes = salt.encode('utf-8')
    else:
        raise TypeError("Salt must be either str or bytes")
    password_bytes = password.encode('utf-8')
    combined = salt_bytes + password_bytes
    hash_digest = hashlib.sha256(combined).hexdigest() #hash the password with salt
    return f"{salt_bytes.hex()}${hash_digest}" #format salt$hash

#Validate user input
def validate_input(input):
    if ',' in input:
        print("[Input cannot contain ',' (comma), please try again!]")
        return False
    elif ' ' in input:
        print("[Input cannot contain ' ' (space), please try again!]")
        return False
    elif '/' in input:
        print("[Input cannot contain '/' (slash), please try again!]")
        return False
    elif "'" in input:
        print('[Input cannot contain "\'" (single quote), please try again!]')
        return False
    elif '"' in input:
        print("[Input cannot contain '\"' (double quote), please try again!]")
        return False
    elif ':' in input:
        print("[Input cannot contain ':' (colon), please try again!]")
        return False
    elif not input:
        print("[Input cannot be empty, please try again!]")
        return False
    return True

#Validate email address, ensure the email input contain '@'
def validate_email(input):
    if '@' not in input:
        print("[Invalid email address format, please try again with an email contain '@'!]")
        return False
    return True

#Communication between Client and Server
def send_cmd(command):
    clientSocket.send(command.encode())
    response = clientSocket.recv(1024).decode() #get response from server
    print(response)
    return response

#Allow the system to stop waitng for input from user's keyboard when there is timeout (used when OTP expires)
def input_with_timeout(message, timeout):
    print(message, end='', flush=True) #Print the message without a newline
    result = [] #store user input
    start_time = time.time() #Record the current time to measure elapsed time later
    
    #Looping over and checking every key pressed by user
    while True: 
        if msvcrt.kbhit():  #[Windows only] Checks if a key has been pressed
            char = msvcrt.getwch() #Get the pressed key without echoing it to the console
            if char == '\r':  #Enter
                print() #Print a newline
                return ''.join(result) #Return all the keys collected as a string
            elif char == '\x08':  #Backspace
                if result: #If result is not empty
                    result.pop() #Remove the last character
                    #Handle backspace in terminal
                    sys.stdout.write('\b \b') #Moving cursor back, erase *, move cursor back
                    sys.stdout.flush()
            else: #Regular keys
                sys.stdout.write('*') #Print * (masking the input)
                sys.stdout.flush()
                result.append(char) #Add the key to the result list
        
        #Check if elapsed time exceeds the timeout
        if time.time() - start_time > timeout:
            print("\n=======================================")
            return None #Indicate timeout occurred

#OTP: Email verification process (to facilitate communication between system and client)
def email_verification_process(username_or_email,userType,commandNext):
    #Send the request to server to autheticate user and provide requested service
    cmd = "AuthenticateUserID,%s,%s,%s" % (username_or_email,userType,commandNext)

    #Email verification process and get user's identity authentication result from server
    print("=====[Please wait for the Verification Email.]=====")
    send_cmd(cmd) #receive system notice of sent email (also ask user to check mailbox)

    while True:
        try:
            #Set a 1-minute (60 seconds) timeout for sending the verification code (to match the timeout set in server)
            #Get input with 60-second timeout
            v_code = input_with_timeout('Enter your 6-digit Verification Code: ', 60)
            
            if v_code is None:  # Timeout occurred
                raise TimeoutError("Verification timed out")

            #Check if the code is valid (6 digits only)
            if len(v_code) != 6:
                print("[You should enter a 6-digit code, please try again.]")
                continue
            elif v_code.isdigit() == False:
                print("[The code only contains numbers, please try again.]")
            #send v_code, get and print response from server to see if the verification code sent to Server is correct
            send_cmd(v_code)
            
            # case 1/2/3/4 (please refer to email_verification() in System_server.py)
            sys_message = clientSocket.recv(1024).decode() 
            #case 1: system returns true (Authenticates user's identity: v_code from user input is corret or it is correct at last attempt before all attempts are used up)
            if sys_message == "True": 
                return True
            #case 3: verification failed and system declines the request
            elif sys_message.startswith("[Your"):
                print(sys_message) #print request declined message from server
                return False
            #case 2: system returns system notice of sent email (v_code from user input is incorret and user still has quota to make attempts)
            else: 
                print("=====[New Veriication Email will be sent soon. Please wait a while.]=====")
                print(sys_message) #print system notice of sent email
        #case 4: timeout occurred (1 minute passed)
        except TimeoutError: 
            timeout_message = clientSocket.recv(1024).decode()
            if timeout_message.endswith("[Verification process failed.]"):
                print(timeout_message)
                sys_message = clientSocket.recv(1024).decode() 
                print(sys_message) #print request declined message from server
                return False
            else:
                print(timeout_message)
                print("=====[New Veriication Email will be sent soon. Please wait a while.]=====")
                sys_message = clientSocket.recv(1024).decode() 
                print(sys_message) #print system notice of sent email

#Change password
def change_password(username, userType, clientSocket):
    new_pw = getpass.getpass("Enter your new password: ") #Password input is not viewable in terminal
    if validate_input(new_pw) == False:
        return
    while len(new_pw) < 8: #ensure the new password is strong
        print("[Your password is too short, please try again with password of at least 8 characters.]\n")
        new_pw = getpass.getpass("Enter your password again: ")
    new_pw2 = getpass.getpass("Verify your password: ")
    verify_time = 2 #limit the verify time to 3
    while (new_pw != new_pw2):
        if verify_time > 0:
            print("[Please make sure your passwords are match!]")
            new_pw2 = getpass.getpass("Verify your password again: ")
            verify_time = verify_time-1
        else: #Verification fail, tell server to log the failure
            print("[Unable to change password, please try again.]")
            cmd = "changePW,%s,%s,%s,%s" % (username,userType,"NIL","NIL")
            clientSocket.send(cmd.encode())
            return
    salt = os.urandom(16)
    hash_pass = hash_password_salt(salt, new_pw)
    cmd = "changePW,%s,%s,%s,%s" % (username,userType,hash_pass,salt) #send command to server for changing password
    send_cmd(cmd)

#Admin unlock user
def unlock_user(username):
    unlockName = input("Enter the username to unlock: ")
    if validate_input(unlockName) == False:
        return
    userType = input("Enter the corresponding user type (User/Admin): ")
    while userType != "User" and userType != "Admin":
        userType = input("Please enter 'User' or 'Admin': ")
    cmd = "Unlock,%s,%s,%s" % (username,unlockName,userType)
    send_cmd(cmd)

#Log in function
def login(userType):
    #USERNAME
    #Get username
    username = input("Enter your username: ")
    #Validate username
    while validate_input(username) == False:
        username = input("Enter your username again: ")

    login_attempts = 3 #Maximum number of login attempt
    attempt_counter = 0 #number of attempt 
    success_login = False

    while attempt_counter<=login_attempts and not success_login:
        if userType == "User": #User log in
            cmd = "userLogin,%s" % (username) #send command to server for getting salt and number of remaining attempts
        else: #Admin log in
            cmd = "adminLogin,%s" % (username) #send command to server for getting salt and number of remaining attempts
        clientSocket.send(cmd.encode())
        Login_response = clientSocket.recv(1024).decode()

        if Login_response.startswith("ERROR"):
            print(Login_response)
            continue
        elif Login_response.startswith("[Your account is locked]") or Login_response.startswith("[User not found]"): #account is locked, unable to log in
            print(Login_response)
            break
        try:
            salt, login_attempts = Login_response.split(',') 
        except:
            print("[User not found. Please try again.]")
            break
        login_attempts = int(login_attempts) #remaining log in attempts saved in database

        #PASSWORD
        #Get Password
        password = getpass.getpass("Enter your password: ")
        #Validate Password
        while validate_input(password) == False:
            password = getpass.getpass("Enter your password again: ")

        attempt_counter += 1
        hashed_password = hash_password_salt(salt, password) #hash password with stored salt
        clientSocket.send(hashed_password.encode())
        response = clientSocket.recv(1024).decode() #log in response 

        success_response = "\nLogin successfully!\n" + "Welcome back "+username+"!\n"
        if response.strip() == success_response.strip(): #log in successfully, go to user interface
            #Email verification process and get user's identity authentication result from server
            if email_verification_process(username,userType,"login"): #authenticated
                print(response)
                if userType == "User":
                    userInterface(username,clientSocket)
                else:
                    adminInterface(username,clientSocket)
                success_login = True
            
        elif response.startswith("[Incorrect password"): #log in fail
            attempt_remain = login_attempts-1 #update remaining attempt
            cmd = "Attempt,%s,%s,%d" % (userType,username,attempt_remain)
            clientSocket.send(cmd.encode())
            if attempt_remain>0: #number of remaining attempt > 0
                print("[You have %d attempts left. Your account will be locked if remaining attempt equal to 0.]\n" % attempt_remain)
            else:
                cmd = "Lock,%s,%s" % (userType,username) #lock the account
                clientSocket.send(cmd.encode())
                print("[You have tried 3 times, account will be locked.]")
                print("[Please contact our admin to unlock your account.]")
                break

def userInterface(username,clientSocket):
    #Refresh remaining log in attempt after log in successfully
    refreshAttempt = "Refresh,User,%s" % (username) 
    clientSocket.send(refreshAttempt.encode())

    while True:
        print("-------------User Interface-------------\n"
            "1. Upload your file.\n"
            "2. Download your file.\n"
            "3. Edit your file.\n"
            "4. Delete your file.\n"
            "5. Share your file with designated users.\n"
            "6. Change your password.\n"
            "7. Log out.\n"
            "----------------------------------------")
        func = input("Enter the number: ")
        
        if func == '1': #upload
            filename = input("Enter the filename you want to upload: ")
            #Check valid file name
            check = filename[:2]
            if check == '../':
                print("Invalid filename!")
                print("Do not use filename start with '../'.")
                continue
            if validate_input(filename) == False:
                continue
            
            #Check if file exists locally first
            if not os.path.exists(filename):
                print(f"[File " + filename + " not found locally]")
                continue
            
            #Check filename uniqueness on server
            check_cmd = "Checkfilename,%s,%s" % (filename,username)
            clientSocket.send(check_cmd.encode())
            check_response = clientSocket.recv(1024).decode().strip()
            
            if check_response == "False":
                print("[File " + filename + " already exists in your database]")
                continue
            
            try:
                #Encrypt the file
                encrypted_data, iv_hex, key_hex = encryptFile(filename)
                if not all([encrypted_data, iv_hex, key_hex]):
                    print("[File encryption failed]")
                    continue
                    
                #Send metadata first
                cmd = "Upload,%s,%s,%s,%s" % (filename,username,iv_hex,key_hex)
                clientSocket.send(cmd.encode())
                
                #Wait for server ready signal
                ready = clientSocket.recv(1024).decode()
                if ready != "READY_FOR_DATA":
                    print("[Server not ready for data]")
                    continue
                    
                #Send binary data length first (8 bytes)
                data_len = len(encrypted_data)
                clientSocket.send(data_len.to_bytes(8, 'big'))
                
                #Send binary data in chunks
                chunk_size = 65536
                sent = 0
                while sent < data_len:
                    chunk = encrypted_data[sent:sent+chunk_size]
                    clientSocket.send(chunk)
                    sent += len(chunk)
                    
                #Get server response
                response = clientSocket.recv(1024).decode()
                print(response)
            
            except Exception as e:
                print(f"[Upload failed: " + str(e) + "]")

                    
        elif func == '2': #download
            #Email verification process and get user's identity authentication result from server
            if email_verification_process(username,"User","Download"): #User is authenticated
                filename = input("Enter the filename you want to download: ")
                if validate_input(filename) == False:
                    continue
                
                cmd = "Download,%s,%s" % (username,filename)
                clientSocket.send(cmd.encode())
                
                try:
                    # Receive metadata length (4 bytes)
                    metadata_len = int.from_bytes(clientSocket.recv(4), 'big')
                    # Receive metadata
                    metadata = clientSocket.recv(metadata_len).decode('utf-8')
                    try:
                        iv_hex, key_hex = metadata.split(',')
                    except:
                        print("["+metadata[3:])
                        continue
                    
                    # Receive file length (8 bytes)
                    file_len = int.from_bytes(clientSocket.recv(8), 'big')
                    # Receive file data
                    file_data = bytearray()
                    while len(file_data) < file_len:
                        chunk = clientSocket.recv(min(4096, file_len - len(file_data)))
                        if not chunk:
                            break
                        file_data.extend(chunk)
                        print(f"[Downloaded {len(file_data)}/{file_len} bytes]", end='\r')
                    
                    if len(file_data) != file_len:
                        print("\n[Download incomplete]")
                        continue
                        
                    print("\n[Download complete]")
                    
                    # Decrypt and save file
                    decrypted_data = decryptFile(file_data, iv_hex, key_hex)
                    if decrypted_data is None:
                        print("[Decryption failed]")
                        continue
                        
                    with open(filename, 'wb') as f:
                        f.write(decrypted_data)
                        
                    print("[File saved successfully!Please check it in your loacl file.]")
                    
                except Exception as e:
                    print("[Download failed: " + str(e) + "]")

        elif func == '3': #edit
            filename = input("Enter the filename you want to edit: ")
            check = filename[:2]
            if check == '../':
                print("[Invalid filename!]")
                print("[Do not use filename start with '../'.]")
                continue
            if validate_input(filename) == False:
                continue
                
            upload_filename = input("Enter the filename containing the data you want to upload to "+ filename +": ")
            
            try:
                #Check if source file exists
                if not os.path.exists(upload_filename):
                    print("[File " + upload_filename + " not found]")
                    continue
                    
                #Encrypt the new file content
                encrypted_data, iv_hex, key_hex = encryptFile(upload_filename)
                if not all([encrypted_data, iv_hex, key_hex]):
                    print("[File encryption failed]")
                    continue
                    
                #Send edit command with metadata
                cmd = "Edit,%s,%s,%s,%s" % (filename,username,iv_hex,key_hex)
                clientSocket.send(cmd.encode())
                
                #Wait for server ready signal
                ready = clientSocket.recv(1024).decode()
                if ready != "READY_FOR_DATA":
                    print("[Server not ready for data]")
                    continue
                    
                #Send binary data length first (8 bytes)
                data_len = len(encrypted_data)
                clientSocket.send(data_len.to_bytes(8, 'big'))
                
                #Send binary data in chunks
                chunk_size = 65536
                sent = 0
                while sent < data_len:
                    chunk = encrypted_data[sent:sent+chunk_size]
                    clientSocket.send(chunk)
                    sent += len(chunk)
                    
                #Get server response
                response = clientSocket.recv(1024).decode()
                print(response)
            except Exception as e:
                print("[Edit failed: " + str(e) + "]")

        elif func == '4': #delete
            filename = input("Enter the filename you want to delete: ")
            check = filename[:2]
            if check == '../':
                print("[Invalid filename!]")
                print("[Do not use filename start with '../'.]")
                continue
            if validate_input(filename) == False:
                continue
            cmd = "Delete,%s,%s" % (username,filename)
            send_cmd(cmd)
        
        elif func == '5': #share files
            #Email verification process and get user's identity authentication result from server
            if email_verification_process(username,"User","ShareFile"): #User is authenticated
                filename = input("Enter the filename you want to share: ")
                #Validate filename
                check = filename[:2]
                if check == '../':
                    print("[Invalid filename!]")
                    print("[Do not use filename start with '../'.]")
                    continue
                if validate_input(filename) == False:
                    continue
                #Get usernames whom user want to share the file with
                otherUsernames = []
                print("[Please enter all the username(s) whom you want to share with ONE BY ONE, including those you entered before.]")
                while True:
                    otherUsernameInput = input("Enter a username [enter /// (3 slashes) to finish]: ").strip()
                    if otherUsernameInput == "///":  #If user enters "///"
                        break
                    if validate_input(otherUsernameInput) == False:
                        continue
                    otherUsernames.append(otherUsernameInput)
                cmd = "ShareFile,%s,%s,%s" % (username,filename,"/".join(otherUsernames))
                send_cmd(cmd)

        elif func == '6': #change password
            #Email verification process and get user's identity authentication result from server
            if email_verification_process(username,"User","changePW"): #User is authenticated
                change_password(username, "user",clientSocket)
            
        elif func == '7': #log out
            print("Goodbye!\n")
            cmd = "logout,User,%s" % (username)
            clientSocket.send(cmd.encode())
            break
        else:
            print("[Invalid input. Please enter 1/2/3/4/5/6/7.]")

def adminInterface(username,clientSocket):
    #Refresh remaining log in attempt after log in successfully
    refreshAttempt = "Refresh,Admin,%s" % (username) 
    clientSocket.send(refreshAttempt.encode())

    while True:
        print("------Administrator Interface------\n"
            "1. Read log file.\n"
            "2. Change your password.\n"
            "3. Unlock user.\n"
            "4. Log out.\n"
            "-----------------------------------")
        func = input("Enter the number: ")
        if func == '1': #read log file
            #Email verification process and get admin's identity authentication result from server
            if email_verification_process(username,"Admin","Log"): #Admin is authenticated
                cmd = "Log,%s" % (username)
                clientSocket.send(cmd.encode())
                while True:
                    chunk = clientSocket.recv(4096).decode()
                    if not chunk:  #No more data
                        break
                    if "FINISH" in chunk:
                        print(chunk.replace("FINISH", ""), end='') #Print remaining data before termination marker
                        break
                    print(chunk, end='')  #Print as we receive

        elif func == '2': #change password
            #Email verification process and get admin's identity authentication result from server
            if email_verification_process(username,"Admin","changePW"): #Admin is authenticated
                change_password(username, "admin",clientSocket)

        elif func == '3': #unlock user
            #Email verification process and get admin's identity authentication result from server
            if email_verification_process(username,"Admin","Unlock"): #Admin is authenticated
                unlock_user(username)

        elif func == '4': #log out
            print("Goodbye!\n")
            cmd = "logout,Admin,%s" % (username)
            clientSocket.send(cmd.encode())
            break
        else:
            print("[Invalid inpit. Please enter 1/2/3/4.]")

def main_menu():
    while True:
        try:
            print("------------------------Main Menu-------------------------\n"
            "1. Register\n"
            "2. Login\n"
            "3. Exit\n"
            "----------------------------------------------------------")
            option = input("Enter the number: ")

            if option == '1': #1. Register
                #USERNAME
                #Get username
                username = input("Enter your username: ")

                #Check if username is valid
                while validate_input(username) == False:
                    username = input("Enter your username again: ")
                while len(username) > 15: #limit username length to 15 character
                    print("[Your username is too long, please try again with username of at most 15 character.]\n")
                    username = input("Enter your username again: ")

                #Check if the username exists in the database
                cmd = "register,%s,%s,%s,%s" % (username, "NIL", "NIL", "NIL") 
                clientSocket.send(cmd.encode())
                validName = clientSocket.recv(1024).decode()
                if validName.startswith("[Username"):
                    print(validName)
                    continue

                #PASSWORD
                #Get user password (input cannot be seen by anyone)
                password = getpass.getpass("Enter your password: ")

                #Check if the password is valid
                while validate_input(password) == False:
                    password = getpass.getpass("Enter your password again: ")
                while len(password) < 8:
                    print("[Your password is too short, please try again with password of at least 8 characters.]\n")
                    password = getpass.getpass("Enter your password again: ")

                #Let user enter the password twice to help them avoid typos in their first entry of the password
                password_valid = getpass.getpass("Verify your password: ")
                verify_time = 2 #limit the verify time to 3
                verified_password = True
                while (password_valid != password):
                    if verify_time > 0:
                        print("[Please make sure your passwords are match!]")
                        password_valid = getpass.getpass("Verify your password again: ")
                        verify_time = verify_time-1
                    else:
                        #Verification failed three times, registration fail 
                        print("[Unable to set password, please register again.]")
                        verified_password = False
                        break
                if verified_password == False: #password is not verified, skip the remaining code
                    continue

                #EMAIL
                #Get user email
                email = input("Enter your email: ")

                #Check if the email is valid
                while validate_input(email)==False or validate_email(email)==False:
                    email = input("Enter your email again: ")

                #Let user enter the email twice to help them avoid typos in their first entry of the email
                email_valid = input("Verify your email: ")
                verifyEmail_time = 2 #limit the verify time to 3
                verified_email = True
                while (email_valid != email):
                    if verifyEmail_time > 0:
                        print("[Please make sure your email are match!]")
                        email_valid = input("Verify your email again: ")
                        verifyEmail_time = verifyEmail_time-1
                    else:
                        #Verification failed three times, registration fail 
                        print("[Unable to set email, please register again.]")
                        verified_email = False
                        break
                if verified_email == False: #password is not verified, skip the remaining code
                    continue    
                
                #Email verification process and get user's identity authentication result from server
                if email_verification_process("email:"+email,"User","register"): #User is authenticated
                    salt = os.urandom(16)
                    hash = hash_password_salt(salt,password) #hash password
                    cmd = "register,%s,%s,%s,%s" % (username, hash, salt, email)
                    send_cmd(cmd) #get and print response (notice of email sent) from server
                continue

            elif option == '2': #2. Login
                while True:
                    print("Who are you?\n"
                    "1. User\n"
                    "2. Administrator\n" \
                    "3. Return to main menu")
                    userType = input("Enter the number: ")

                    #Check if the input is valid
                    if userType not in ['1', '2', '3']: 
                        print("[Error input, please enter 1/2/3]")
                        continue

                    #Go to two diff functions
                    if userType == '1': #User login
                        login("User")
                    elif userType == '2': #Admin login
                        login("Admin")
                    elif userType == '3':
                        break
                    break
            elif option == '3': #3. Exit
                exit(0)
            else: #Invalid input
                print("[Error input, please enter 1/2/3]")
                continue
        except ConnectionResetError: #server shut down
            print("[Disconnected from server. You have been logged out.]")
            exit(0)
        except Exception as e:
            print("[ERROR: %s]" % str(e))

#File encryption by AES CBC before uploading to server
def encryptFile(filename):
    #Read file content in plaintext
    try:
        with open(filename, "rb") as f:
            plaintext = f.read()            
    except Exception as e:
        print("[Error reading file: " + str(e) + "]")
        
    #Generate random key and IV
    AES_k = os.urandom(32)  
    AES_iv = os.urandom(16)  

    #Pad and encrypt
    padder = padding.PKCS7(128).padder()
    padded_data = padder.update(plaintext) + padder.finalize()
    cipher = Cipher(algorithms.AES(AES_k), modes.CBC(AES_iv), backend=default_backend())
    encryptor = cipher.encryptor()
    encrypted_data = encryptor.update(padded_data) + encryptor.finalize()

    return (encrypted_data, AES_iv.hex(), AES_k.hex())  


#File decryption after downloading file from server
def decryptFile(encrypted_data, iv_hex, key_hex):
    try:
        #Convert hex strings to bytes
        iv = bytes.fromhex(iv_hex)
        key = bytes.fromhex(key_hex)
        
        #Verify lengths of iv and key
        if len(key) != 32:
            raise ValueError("Invalid key length. Must be 32 bytes")
        if len(iv) != 16:
            raise ValueError("IV must be exactly 16 bytes")
        
        #Initialize cipher
        cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=default_backend())
        decryptor = cipher.decryptor()
        
        #Decrypt and unpad
        padded_data = decryptor.update(encrypted_data) + decryptor.finalize()
        unpadder = padding.PKCS7(128).unpadder()
        decrypted_data = unpadder.update(padded_data) + unpadder.finalize()       
        return decrypted_data     
    except Exception as e:
        print("Decryption error: " + str(e) + "]")
        return None  

if __name__ == "__main__":
    try:
        clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        serverName = '127.0.0.1'
        serverPort = 12345
        clientSocket.connect((serverName, serverPort))
        print("[connected to server......]\n")
        print("-----------Team 65 Secure Online Storage System-----------")
        print("Welcome to our system!\nPlease enter a number to select required function.\n")
        main_menu()
        clientSocket.close()
    except KeyboardInterrupt:
        print("[The system is closed. Bye-bye!]")
        clientSocket.close()
        exit(0)
    except Exception as e:
        print("[ERROR: %s]" % str(e))