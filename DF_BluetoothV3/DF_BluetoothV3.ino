/*

 DF-BluetoothV3 Bluetooth module
 
 Intefaces the DF-BluetoothV3 Bluetooth module with most Arduino contollers.
 
 When the DF-Bluetooth is used on Arduino, please make sure you disconnect the 
 DF-Bluetooth module before uploading any code to your Arduino. It won’t burn 
 your Arduino, but the uploading will fail as the DF-Bluetooth module occupying 
 the TX/RX pins.
 
 * Copyright (c) 2012 by Tony Guntharp. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 
 */

void setup() 
{
  // Initialize serial communications: Set serial baud rate to 9600
  Serial.begin(9600);          
} 

void loop() 
{
  // Print out our Hello World string followed by a newline
  Serial.print("Hello World from the DF-BluetoothV3 Bluetooth module");        
  Serial.println();
  // 1 second delay
  delay(1000);                  
}
