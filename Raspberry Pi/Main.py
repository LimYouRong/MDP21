from AndroidConnection import *
from PCConnection import *
from ArduinoConnection import *

import sys
import time
import threading
import capture

coords = "0"

class RPi(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.debug = False

        #Initialization of component
        self.android_thread = bluetooth()
        self.pc_thread = pcComm()
        self.arduino_thread = arduino()

        self.android_thread.btComms()
        self.pc_thread.pcComms()
        self.arduino_thread.arComms()

        time.sleep(2)

    
    def sendArduino(self, arduinomsg):
        if (self.arduino_thread.arduinoConnected and arduinomsg):
            print(arduinomsg)
            self.arduino_thread.writeArduino(arduinomsg)
            return True
        return True
    
    def receiveArduino(self):
        while True:
            serialmsg = self.arduino_thread.readArduino()
            serialmsgArray = serialmsg.split(",")
            header = serialmsgArray[0]
            
            #Format Dest,Source,Value
            if (self.arduino_thread.arduinoConnected and serialmsg):
                if(header == 'PC'):                                         #E.g. PC,AR,Sensor_Data
                    new = serialmsgArray[2].rstrip("\n")		    #Remove newline for algo
                    self.sendPC(new)
                elif(header == 'AN'):                                       #E.g. AN,OBS|Sensor_Data|X|Y|DIR
                    new2 = serialmsgArray[1].rstrip("\n")
                    self.sendAndroid(new2)
                else:
                    print("Incorrect device selected from Arduino: %s" %(serialmsg))

    def sendPC(self, pcmsg):
        if (self.pc_thread.pcConnected() and pcmsg):
            self.pc_thread.sendPC(pcmsg)
            return True
        return False

    def receivePC(self):
            while True:
                pcmsg = self.pc_thread.readPC()
                pcmsg = str(pcmsg)
                pcmsgArray = pcmsg.split(",")
                header = pcmsgArray[0]

                if (self.pc_thread.pcConnected and pcmsg):              #Format Dest,Source,Value
                    if(header == 'AN'):                                     #E.g. AN,PC,MDF|...
                        self.sendAndroid(pcmsgArray[2])
                    elif(header == 'AR'):                                           #E.g. AR,PC,Instructions_to_move
                        self.sendArduino(pcmsgArray[2])
                    elif(header == 'P'):
    	                coords = pcmsgArray[1] + "," + pcmsgArray[2] + "," + pcmsgArray[3]  #P,X,Y,DIR
	                capture.capture(coords)					    #Takes a picture and saves into directory
                        self.sendPC('cont')                                       #sends to pc to carry out next instruction
                    elif(header == 'C'):
                        self.sendAndroid(pcmsgArray[2])			    # send coordinates to arduino 
                else:
                    print("Incorrect message received from PC: %s" %(pcmsg))

    def sendAndroid(self,androidmsg):
        if (self.android_thread.checkConnection() and androidmsg):            
            self.android_thread.writeTablet(androidmsg)
            return True
        return False

    def receiveAndroid(self):
        while True:
            btmsg = self.android_thread.readTablet()
            btmsg = str(btmsg)
            btmsgArray = btmsg.split(",")
            header = (btmsgArray[0])
            
            #Format Dest,Source,Value
            if (self.android_thread.checkConnection() and btmsg != "None"):
                if(header == 'PC'):
                    if (len(btmsgArray)>3):                                 #E.g. PC,AN,X,Y
                        self.sendPC(btmsgArray[2]+","+btmsgArray[3])
                    else:                                                   #E.g. PC,AN,startfp
                        self.sendPC(btmsgArray[2])
                elif(header == 'AR'):                                       #E.g. AR,AN,1
                    self.sendArduino(btmsgArray[2])
                else:
                    print("Incorrect device selected from Android: %s" %(btmsg))


    def beginThread(self):
        #PC read and write thread
        read_pc_thread = threading.Thread(target = self.receivePC, args = (), name = "read_pc_thread")
        write_pc_thread = threading.Thread(target = self.sendPC, args = (), name = "write_pc_thread")
        
        #Android read and write thread
        read_android_thread = threading.Thread(target = self.receiveAndroid, args = (), name = "read_android_thread")
        write_android_thread = threading.Thread(target = self.sendAndroid, args = (), name = "write_android_thread")

        #Arduino read and write thread
        read_arduino_thread = threading.Thread(target = self.receiveArduino, args = (), name = "read_arduino_thread")
        write_arudino_thread = threading.Thread(target = self.sendArduino, args = (), name = "write_arudino_thread")

        #Set daemon for all thread
        read_pc_thread.daemon = True
        write_pc_thread.daemon = True

        read_android_thread.daemon = True
        write_android_thread.daemon = True

        read_arduino_thread.daemon = True
        write_arudino_thread.daemon = True
        
        #start running the thread for PC
        read_pc_thread.start()

        #start running thread for Android
        read_android_thread.start()

        #start running thread for Arduino
        read_arduino_thread.start()


    def close_all(self):
        self.pc_thread.closePCSocket()
        self.android_thread.disconnect()
        self.arduino_thread.closeserial()

    def maintainThread(self):
        while True:
            time.sleep(1)

if __name__ == "__main__":
    #Start script
    print("Starting...")
    main = RPi()

    try:
        main.beginThread()
        main.maintainThread()
    except KeyboardInterrupt:
        print("Closing...")
        main.close_all()
