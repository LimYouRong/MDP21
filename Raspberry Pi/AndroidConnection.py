from bluetooth import *
import threading
import time
import sys
import os

class bluetooth(object):

    def __init__(self):
        self.serverSoc = None
        self.clientSoc = None
        self.connection = False

    def checkConnection(self):
        return self.connection

    def btComms(self):
        port = 7
        try:
                self.serverSoc = BluetoothSocket( RFCOMM )
                self.serverSoc.bind(("",port))
                self.serverSoc.listen(1)
                self.port = self.serverSoc.getsockname()[1]
                uuid= "163660a6-ad17-44fc-99c5-5c75e78ad815"

                advertise_service( self.serverSoc, "MDP-Server",
                                    service_id = uuid,
                                    service_classes=[ uuid, SERIAL_PORT_CLASS ],
                                    profiles = [ SERIAL_PORT_PROFILE ],)
                print ("Waiting for BT connection on RFCOMM channel %d" % self.port)
                self.clientSoc, clientinfo = self.serverSoc.accept()
                print ("Accepted connection from" , clientinfo)
                self.connection = True

        except Exception as e:
                print ("Error: %s" % str(e))
                self.disconnect()
    
    def writeTablet(self,message):
        try:
            self.clientSoc.send(str(message))
            print('Sent to Android: %s' % message)
        except BluetoothError:
            print ("Bluetooth Error! Reconnecting.")
            self.btComms()
    
    def readTablet(self):
        try:
            msg = self.clientSoc.recv(1024)
            msg = msg.decode('utf-8')
            print("Received from android: %s" % str(msg))
            return(msg)

        except BluetoothError:
            print("Bluetooth error! Connection reset by peer. Trying to connect... ")
            self.btComms()
    
    def disconnect(self):
        self.clientSoc.close()
        print ("closing client socket")
        self.serverSoc.close()
        print ("closing server socket")
        self.connection = False

    def maintain(self):
        while True:
            time.sleep(1)
