import serial
import time
import sys
import os
import threading

class arduino(object):
    def __init__(self):
        # Initialize ArduinoObj
        self.port = '/dev/ttyACM0'
        self.baudrate = 9600
        self.serCon = None
        self.arConnect = False

    def arComms(self):
        # Create socket for the serial port
        print("Awaiting Arduino Connection...")
        while True:
            retry = False
            try:
                self.serCon = serial.Serial(self.port,self.baudrate)
                self.arConnect = True
                print("Serial link connected")
                retry = False
            except Exception as e:
                print ("Error (Serial): %s " % str(e))
                rety = True
            if(not retry):
                break
            print("Retrying Arduino Connection...")
            time.sleep(2)

    def arduinoConnected(self):
        return self.arConnect


    def writeArduino(self,msg):
        try:
            self.serCon.write(str.encode(msg))
            print ("Sent to arduino: %s" % msg)
        except AttributeError:
            print("Error in serial comm. No value to be written. Check connection!")

            
    def readArduino(self):
        try:
            received_data = self.serCon.readline()
            received_data = received_data.decode('utf-8')
            received_data = str(received_data)
            message = received_data.split(",")                          #E.g. AN/PC,AR,OBS|Sensor_Data
            print ("Received from arduino: %s" % message[1])
            return received_data

        except Exception as e:
            print("Error in serial comm. No value received. Check connection! %s"%(str(e)))

    def closeserial(self):
        # Closing socket
        self.serCon.close()
        self.arConnect = False
        print("Closing serial socket")
