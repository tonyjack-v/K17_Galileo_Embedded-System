void setup() {
Serial.begin(9600);

}

void loop() {
   //Display ifconfig result to serial monitor
  system("ifconfig > /dev/ttyGS0");
  delay(2000);
  }
