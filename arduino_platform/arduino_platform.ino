/*
 The circuit: 
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 */
#include <SoftwareSerial.h>
#include <AFMotor.h>
#include <math.h>

AF_DCMotor leftMotor(1);
AF_DCMotor rightMotor(2);

SoftwareSerial btSerial(10, 11); // RX, TX

int currPos;
char temp[15];
char dir;

#define FATOR_MOTOR 2.5

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
  
  leftMotor.run(RELEASE);
  rightMotor.run(RELEASE);
}



void loop() // run over and over
{
  if (btSerial.available())
  {
    char c = btSerial.read();
    
    //Serial.print( "Raw read: " );
    //Serial.println( c );
    
    if(currPos == -1)
    {
      dir = c;
      currPos = 0;
      return;
    }
    
    if(c == 0x13) // DC3
    {
      temp[currPos] = 0;
      int value = atoi( temp ) * FATOR_MOTOR;
      
      Serial.print( "Direcao: " );
      Serial.println( dir );
      Serial.print( "Potencia: " );
      Serial.println( value );
      
      if(dir == 'S')
      {
        leftMotor.run(RELEASE);
        rightMotor.run(RELEASE);
      } 
      else
      {
        if (dir == 'F')
        {
          leftMotor.run(FORWARD);
          rightMotor.run(FORWARD);
        }
        else if (dir == 'B')
        {
          leftMotor.run(BACKWARD);
          rightMotor.run(BACKWARD);
        }
        else if (dir == 'L')
        {
          leftMotor.run(BACKWARD);
          rightMotor.run(FORWARD);
        }
        else if (dir == 'R')
        {
          leftMotor.run(FORWARD);
          rightMotor.run(BACKWARD);
        }
          
        leftMotor.setSpeed( value );
        rightMotor.setSpeed( value );
      }
      
      currPos = -1;
    }  
    else
      temp[currPos++] = c;
  }
  if (Serial.available())
    btSerial.write(Serial.read());
}

