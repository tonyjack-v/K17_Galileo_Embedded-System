import mraa
import time
import sys
import threading
from firebase import firebase
from uuid import getnode as get_mac
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import cgi
import socket
import fcntl
import struct

PORT_NUMBER = 9000

def get_ip_address(ifname):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(s.fileno(),
        0x8915,
        struct.pack('256s', ifname[:15])
        )[20:24])

class FireBase:
    def __init__(self, url):
        try:
            self.fire = firebase.FirebaseApplication(url, None)
        except:
            print('Can not connect to firebase')
    def PUT_SENSOR(self):
        global data_sensor
        try:
            result = self.fire.put('/CESLAB', '/GALILEO-2/SENSOR/', data_sensor)
        except:
            print('Can not put data sensor to the firebase')
    def PUT_LED(self):
        global data_led
        try:
            result = self.fire.put('/CESLAB', '/GALILEO-2/LED/', data_led)
        except:
            print('Can not put data led to the firebase')
    def GET(self):
        try:
            result = self.fire.get('/CESLAB', '/GALILEO-2/LED/LED')
            return result
	except:
	    print('Can not get data from firebase')

class MyHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        """Respond to a GET request."""
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write('''<html>''')
        self.wfile.write("<p> <h2>Sensors static:</h2> %s </p>" % self.read_stat())
        self.wfile.write("<body>")
        self.wfile.write("<p>Control the LED:</p>")
        self.wfile.write('''<form method="POST">''')
        self.wfile.write('''<input type="submit" name='cmd' value="Toggle led"/>''')
        self.wfile.write('''<input type="submit" name='cmd' value="Update data"/>''')
        self.wfile.write('</form>')
        self.wfile.write("</body></html>")
    def do_POST(self):
        global toggle_led
        toggle_led = 0
        form = cgi.FieldStorage(
            fp = self.rfile,
            headers = self.headers,
            environ = {'REQUEST_METHOD':'POST',
                        'CONTENT_TYPE':self.headers['CONTENT-Type']
                    }
        )
        if(form["cmd"].value=="Toggle led"):
            toggle_led = 1
            print('Change led status')

        self.do_GET()
    def read_stat(self):
        global data_sensor
        global led
        global toggle_led
        if(toggle_led == 1):
            if(led == 0):
                led = 1
            else:
                led = 0
            toggle_led = 0
        s = '<h3>MAC Address: ' + str(data_sensor['MAC_Address']) + '</h3>\
            <h3>Temperature: ' + str(data_sensor['Temperature']) + ' *C</h3>\
            <h3>Humidity: ' + str(data_sensor['Humidity'])  + ' %</h3>\
            <h3>LED: ' + str(led) + '</h3>\
            <h3>Rain: ' + str(data_sensor['Rain']) +'</h3>'
        return s

class WebServerThread(threading.Thread):
    def __init__(self):
    	threading.Thread.__init__(self)
        self.ip = get_ip_address('enp0s20f6')
        time.sleep(3)
    def run(self):
        httpd = HTTPServer((self.ip, PORT_NUMBER), MyHandler)
        print time.asctime(), "Server Starts - %s:%s" % (self.ip, PORT_NUMBER)
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            pass
        httpd.server_close()
        print(time.asctime(), "Server Stops - %s:%s" % (self.ip, PORT_NUMBER))

def isr_routine_button(gpio):
    time.sleep(0.5)
    global toggle_led
    toggle_led = 1

def isr_routine_rain(gpio):
    global is_raining
    is_raining = 1

class FirmWareThread(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        led = 0
        mac = get_mac()
        self.hex_mac = ':'.join(("%012X" % mac)[i:i+2] for i in range(0, 12, 2))

    def run(self):
        global is_raining
        global data_sensor
        global data_led
        global led
        global toggle_led

        is_raining = 0
        led = 0
        toggle_led = 0
        heartbeat = 0

        fb = FireBase('https://pythonfirebase-5d150-default-rtdb.firebaseio.com/')

        gpio_led = mraa.Gpio(4)
        gpio_led.dir(mraa.DIR_OUT)

        gpio_button = mraa.Gpio(7)
        gpio_button.dir(mraa.DIR_IN)

        gpio_rain = mraa.Gpio(8)
        gpio_rain.dir(mraa.DIR_IN)

        try:
            gpio_button.isr(mraa.EDGE_BOTH, isr_routine_button, gpio_button)
            gpio_rain.isr(mraa.EDGE_BOTH, isr_routine_rain, gpio_rain)
        except e:
            print(e)
        
        try:
            adc_humi = mraa.Aio(0)
            adc_temp = mraa.Aio(1)
        except e:
            print(e)

        while True:
            print('Toggole led in thread', toggle_led)
            if(toggle_led == 1):
                if(led == 0):
                    led = 1
                else:
                    led = 0
                data_led = {'LED': led}
                fb.PUT_LED()
                toggle_led = 0

            print('Led status', led)
            if(led == 0):
                print('Tat')
                gpio_led.write(0)
            else:
                print('Bat')
                gpio_led.write(1)
            try:
                humi = round(adc_humi.read() * 5.0 / 1024.0 * 33.3, 2)
                temp = round(adc_temp.read() * 5.0 / 1024.0 * 100, 2)
            except e:
                print(e)
            
            heartbeat += 1
            if heartbeat >=255:
                heartbeat = 0
            data_sensor = {'Temperature' : temp,
            'Humidity': humi,
            'Rain': is_raining,
            'MAC_Address': self.hex_mac
            }
            fb.PUT_SENSOR()
            if(toggle_led == 0):
                led = fb.GET()
                print 'led from firebase', led
            
            if(humi == 0 or temp == 0):
                gpio_led = mraa.Gpio(4)
                gpio_led.dir(mraa.DIR_OUT)
                
                gpio_button = mraa.Gpio(7)
                gpio_button.dir(mraa.DIR_IN)
                
                gpio_rain = mraa.Gpio(8)
                gpio_rain.dir(mraa.DIR_IN)
                
                try:
                    print("Starting ISR for button at pin 7")
                    gpio_button.isr(mraa.EDGE_BOTH, isr_routine_button, gpio_button)
                    print("Starting ISR for rain sensor at pin 8")
                    gpio_rain.isr(mraa.EDGE_BOTH, isr_routine_rain, gpio_rain)
                except e:
                    print(e)
                
                try:
                    adc_humi = mraa.Aio(0)
                    adc_temp = mraa.Aio(1)
                except e:
                    print(e)
           # time.sleep(5)

if __name__ == '__main__':
    f_dns_sv = open('/etc/resolv.conf','w+')
    f_dns_sv.write('nameserver 168.95.1.1\nnameserver ::1')
    f_dns_sv.close()

    firmware = FirmWareThread()
    web = WebServerThread()
    
    firmware.start()
    web.start()

#    firmware.join()
#    web.join()
