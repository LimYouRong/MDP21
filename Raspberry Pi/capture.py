from picamera import PiCamera
import os
import Main

#init camera
camera = PiCamera()
camera.close()

def capture(coords):
  camera = PiCamera()					#Start camera
  camera.resolution = (1280,720)			#Same resolution as train images
  camera.capture('/home/pi/Desktop/share/' + coords + '.jpg')
  camera.close()					#Stop camera

  os.system ("sudo mv /home/pi/Desktop/share/* /mnt/share") #Transfer file to mount, for image reg
