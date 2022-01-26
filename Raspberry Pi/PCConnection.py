import socket
import sys
import time
import threading

class pcComm(object):

    def __init__(self):
        self.ip_address = '192.168.21.1'
        self.port = 1212
        self.pcConnect = False

    def closePCSocket(self):
        if self.conn:
            self.conn.close()
            print ('Terminating server socket..')

        if self.client:
            self.client.close()
            print ('Terminating client socket..')
        
        self.pcConnect = False

    def pcConnected(self):
        return self.pcConnect

    def pcComms(self):
        try:
            self.conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.conn.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.conn.bind((self.ip_address, self.port))
            self.conn.listen(1)
            print ('Listening for incoming PC connection on ' + self.ip_address + ':' + str(self.port) + '..')
            self.client, self.addr = self.conn.accept()
            print ("Connected! Connection address: ", self.addr)
            self.pcConnect = True
            
        except Exception as e:
            print ("Error: %s" % str(e))
            print ("Try again in a few seconds")

    def readPC(self):
        try:
            pc_data = self.client.recv(1024)
            pc_data = pc_data.decode('utf-8')
            if (not pc_data):
                self.closePCSocket()
                print('Null transmission from PC; Assuming PC disconnected, trying to reconnect..')
                self.pcComms()
                return pc_data
            print ('Received from PC: ' + pc_data.rstrip())
            return pc_data
        except Exception as e:
            print ('PC Read Error: %s' % str(e))
            self.pcComms()

    def sendPC(self, message):
        try:
                message = str(message)
                writeToPC = str.encode(message + '\n')
                self.client.sendto(writeToPC, self.addr)
                print ('Sent to PC: ' + message)
        except Exception as e:
                print ('PC Write Error: %s' % str(e))
                self.pcComms()