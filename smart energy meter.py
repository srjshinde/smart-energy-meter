import time
from firebase import firebase
import RPi.GPIO  as g
import serial
from datetime import datetime

time.sleep(5)

g.setmode(g.BOARD)                      #initialize pins
g.setup(38, g.IN, pull_up_down=g.PUD_DOWN) #meter pulse
g.setup(40,g.OUT)
g.setup(12,g.OUT)                       #relay

sent= False                             #msg track
#balance1= 200
#reading1= 40
#prev_balance=200
dailycount=0                    #
path=0                  #  path 2018/may/25-05-2018

port= serial.Serial("/dev/ttyUSB0",9600,timeout= 1)

firebase=firebase.FirebaseApplication('https://prepaidm123.firebaseio.com', None)

now=datetime.now()
path=now.strftime("%Y/%B/%d-%m-%Y")

data= firebase.get('/Users',None)

balance1 = data[0]['balance']
balance2 = data[1]['balance']
balance3 = data[2]['balance']
balance4 = data[3]['balance']
balance5 = data[4]['balance']

reading1 = balance1/5					#firebase.get('/reading1',None)
reading2 = balance2/5
reading3 = balance3/5
reading4 = balance4/5
reading5 = balance5/5

if balance1 > 5:
	g.output(12, True)                      #relay
else:
	g.output(12, False)

dailycount=firebase.get('Users/0/'+path,None)
if dailycount==None:
	dailycount=0

print balance1

def gsm_init():
	port.write('AT'+'\r')
	rcv = port.readline()
	print rcv
	time.sleep(1)

	port.write('ATE0'+'\r')      # Disable the Echo
	rcv = port.readline()
	print rcv
	time.sleep(1)

	port.write('AT+CMGF=1'+'\r')  # Select Message format as Text mode
	rcv = port.readline()
	print rcv
	time.sleep(1)

	port.write('AT+CNMI=2,1,0,0,0'+'\r')   # New SMS Message Indications
	rcv = port.readline()
	print rcv
	time.sleep(1)

gsm_init()

def send_sms():

	global sent
	port.write('AT+CMGS= "9175916156"'+'\r')
	rcv = port.readline()
	print rcv
	time.sleep(1)
	#print "hell"
	port.write('Dear customer,\nyour balance is %s \nplease recharge your account soon'%(balance1)+ '\r' )  # Message
	rcv = port.readline()
	print rcv

	port.write("\x1A") # Enable to send SMS
	sent= True
	print 'SMS sent...'


def firebase_update():

    		global balance1, balance2, balance3, balance4, balance5, reading1, reading2, reading3, reading4, reading5
    		firebase.put('','/Users/0/balance',balance1)
		firebase.put('','/Users/0/reading',reading1)
		firebase.put('','/Users/1/balance',balance2)
		firebase.put('','/Users/1/reading',reading2)
		firebase.put('','/Users/2/balance',balance3)
		firebase.put('','/Users/2/reading',reading3)
		firebase.put('','/Users/3/balance',balance4)
		firebase.put('','/Users/3/reading',reading4)
		firebase.put('','/Users/4/balance',balance5)
		firebase.put('','/Users/4/reading',reading5)
		print "done"


def usageupdate():
		global path, dailycount
		now=datetime.now()
		path=now.strftime("%Y/%B/%d-%m-%Y")

		dailycount=dailycount+1

		firebase.put('','/Users/0/'+path,dailycount)
		firebase.put('','/Users/1/'+path,dailycount+1)
		firebase.put('','/Users/2/'+path,dailycount+2)
		firebase.put('','/Users/3/'+path,dailycount+3)
		firebase.put('','/Users/4/'+path,dailycount+4)


def read_pulse():

    global balance1, balance2, balance3, balance4, balance5, reading1, reading2, reading3, reading4, reading5, sent, data
    
    if g.input(38):
	print "got it...\t",datetime.now()

	data= firebase.get('/Users',None)

	balance1 = data[0]['balance']
	balance2 = data[1]['balance']
	balance3 = data[2]['balance']
	balance4 = data[3]['balance']
	balance5 = data[4]['balance']

	reading1 = balance1/5
	reading2 = balance2/5
	reading3 = balance3/5
	reading4 = balance4/5
	reading5 = balance5/5

	if balance1 > 5:
		balance1 = balance1 - 5
       		reading1 = reading1 - 1
		usageupdate()

	if balance2 > 5:
		balance2 = balance2 - 5
       		reading2 = reading2 - 1

	if balance3 > 5:
		balance3 = balance3 - 5
       		reading3 = reading3 - 1

	if balance4 > 5:
		balance4 = balance4 - 5
       		reading4 = reading4 - 1

	if balance5 > 5:
		balance5 = balance5 - 5
       		reading5 = reading5 - 1

        firebase_update()
	

        g.output(40,True)
        time.sleep(0.3)
        g.output(40,False)


	if balance1 > 30:
		sent= False

	if balance1 < 10:
		g.output(12, False)
	else:
		g.output(12, True)

        while g.input(38):
            reading1=reading1


while True:
    try:

        read_pulse()

	if sent== False and balance1 < 30:
		send_sms()
	
	if balance1 < 10:
		balance1= firebase.get("/Users/0/balance",None)
		print balance1
		if balance1 > 10:
			g.output(12, True)

    except IOError:

        print "exited successfully"
