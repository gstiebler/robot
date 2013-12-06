/*
  Software serial multple serial test
 
 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.
 
 The circuit: 
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 
 Note:
 Not all pins on the Mega and Mega 2560 support change interrupts, 
 so only the following can be used for RX: 
 10, 11, 12, 13, 50, 51, 52, 53, 62, 63, 64, 65, 66, 67, 68, 69
 
 Not all pins on the Leonardo support change interrupts, 
 so only the following can be used for RX: 
 8, 9, 10, 11, 14 (MISO), 15 (SCK), 16 (MOSI).
 
 created back in the mists of time
 modified 25 May 2012
 by Tom Igoe
 based on Mikal Hart's example
 
 This example code is in the public domain.
 
 */
#include <SoftwareSerial.h>

SoftwareSerial btSerial(10, 11); // RX, TX

float X, Y, Z;

int state;
int currPos;
char temp[15];
float coords[3];

void setup()  
{
  // Open serial communications and wait for port to open:
  Serial.begin(57600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }

  Serial.println("Goodnight moon!");

  // set the data rate for the SoftwareSerial port
  btSerial.begin(9600);
  btSerial.println("AT\n");
  
  state = 0;
}



void updateMotorState()
{
}



void writeVar()
{
  temp[currPos] = 0;
  float f = atof(temp);
  coords[state++] = f;
  Serial.println(f);
  currPos = 0;
}



void loop() // run over and over
{
  if (btSerial.available())
  {
    char c = btSerial.read();
    if(c == ';' || c == 0x13) // 0x13: DC3
    {
      writeVar();
      
      if(state == 3)
        updateMotorState();
    }
    else if(c == 'A')
    {
      Serial.print("\nStart\n");
      state = 0;
    }
    else
      temp[currPos++] = c;
  }
  if (Serial.available())
    btSerial.write(Serial.read());
}

