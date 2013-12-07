/*
 The circuit: 
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 */
#include <SoftwareSerial.h>
#include <AFMotor.h>
#include <math.h>

AF_DCMotor leftMotor(1);
AF_DCMotor rightMotor(4);

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
  
  leftMotor.run(RELEASE);
  rightMotor.run(RELEASE);
  
  state = 0;
}


void setMotorValue(AF_DCMotor &motor, float value)
{
  if(value > 0)
    motor.run(FORWARD);
  else
    motor.run(BACKWARD);
    
  value = fabs(value * 25.5); // considerando um máximo aprox. de 10.0
  
  if(value > 255)
    value = 255;
    
  motor.setSpeed( value );
  
  Serial.print( "Motor value: ");
  Serial.println( value );
}



void updateMotorState()
{
  float x = coords[0];
  float y = coords[1];
  float z = coords[2];
  
  float leftValue = z + y;
  float rightValue = z - y;
  
  Serial.println( "Motor value: ");
  Serial.println( leftValue );
  Serial.println( rightValue );
  
  setMotorValue( leftMotor, leftValue );
  setMotorValue( rightMotor, rightValue );
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

