/*
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This sketch is an example to understand how Arduino can recognize ALProtocol. 
However, it can easily be reused for their own purposes or as a base for a library. 
Read carefully the comments. When you find "this is general code you can reuse"
then it means that it is generic code that you can use to manage the ALProtocol. 
When you find "this is needed just as example for this sketch" then it means that 
you code useful for a specific purpose. In this case you have to modify it to suit 
your needs.
*/

#define DIGITAL_PIN_COUNT 14 // Change 14 if you have a different number of pins.
#define ANALOG_PIN_COUNT 6 // Change 6 if you have a different number of pins.
#define ANALOG_PIN_NUM_START 14 // Fix analogic pins numerotation (on most common arduinos, it should be equal to DIGITAL_PIN_COUNT)
#define SERIAL_BAUDRATE 115200

boolean digitalPinListening[DIGITAL_PIN_COUNT]; // Array used to know which pins on the Arduino must be listening.
boolean analogPinListening[ANALOG_PIN_COUNT]; // Array used to know which pins on the Arduino must be listening.
int digitalPinListenedValue[DIGITAL_PIN_COUNT]; // Array used to know which value is read last time.
int analogPinListenedValue[ANALOG_PIN_COUNT]; // Array used to know which value is read last time.
int analogPinListenedFrequency[ANALOG_PIN_COUNT];
long analogPinListenedLastRead[ANALOG_PIN_COUNT];
int analogPinListenedThreshold[ANALOG_PIN_COUNT];

void setup() {

  Serial.begin(SERIAL_BAUDRATE);

  // Welcome message
  Serial.print("alp://rply/");
  Serial.print("ok?id=0");
  Serial.print('\n');
  Serial.flush();
  
  // Set to false all listen variable
  int index = 0;
  for (index = 0; index < DIGITAL_PIN_COUNT; index++) {
    digitalPinListening[index] = false;
    digitalPinListenedValue[index] = -1;
  }
  for (index = 0; index < ANALOG_PIN_COUNT; index++) {
    analogPinListening[index] = false;
    analogPinListenedValue[index] = -1;
    analogPinListenedFrequency[index] = 0;
    analogPinListenedLastRead[index] = 0;
    analogPinListenedThreshold[index] = 0;
  }

  // Turn off everything (not on RXTX)
  for (index = 2; index < DIGITAL_PIN_COUNT; index++) {
    pinMode(index, OUTPUT);
    digitalWrite(index, LOW);
  }
  
}

void loop() {
  // when a newline arrives:
  if (Serial.available() > 0) {
    String inputString = Serial.readString();
    if(inputString.startsWith("alp://")) { // OK is a message I know
    
      boolean msgRecognized = true;

      String opcode = inputString.substring(6, 10);

      //Serial.print("opcode:");
      //Serial.println(opcode);
      
      if(opcode == "kprs") { // KeyPressed
        // here you can write your own code. For instance the commented code change pin intensity if you press 'a' or 's'
        // take the command and change intensity on pin 11 this is needed just as example for this sketch
        //char commandChar = inputString.charAt(14);
        //if(commandChar == 'a' and intensity > 0) { // If press 'a' less intensity
        //  intensity--;
        //  analogWrite(11,intensity);
        //} else if(commandChar == 's' and intensity < 125) { // If press 's' more intensity
        //  intensity++;
        //  analogWrite(11,intensity);
        //}
      } else if(opcode == "ppin") { // Power Pin Intensity
          int separatorPosition = inputString.indexOf('/', 11 );
          String pin = inputString.substring(11,separatorPosition);
          String intens = inputString.substring(separatorPosition + 1);
          pinMode(pin.toInt(), OUTPUT);
          analogWrite(pin.toInt(),intens.toInt());
      }
      else if(opcode == "ppsw") { // Power Pin Switch
          int separatorPosition = inputString.indexOf('/', 11 );
          String pin = inputString.substring(11,separatorPosition);
          String power = inputString.substring(separatorPosition + 1);
          pinMode(pin.toInt(), OUTPUT);
          if(power.toInt() == 1) {
            digitalWrite(pin.toInt(), HIGH);
          } else if(power.toInt() == 0) {
            digitalWrite(pin.toInt(), LOW);
          }
      }
      else if(opcode == "tone") { // tone request
          int firstSlashPosition = inputString.indexOf('/', 11);
          int secondSlashPosition = inputString.indexOf('/', firstSlashPosition + 1);
          int pin = inputString.substring(11, firstSlashPosition).toInt();
          int frequency = inputString.substring(firstSlashPosition + 1, secondSlashPosition).toInt();
          int duration = inputString.substring(secondSlashPosition + 1).toInt();
          if(duration == -1) {
          	tone(pin, frequency);
          } else {
          	tone(pin, frequency, duration);
          }
      }
      else if(opcode == "notn") { // no tone request
          int firstSlashPosition = inputString.indexOf('/', 11 );
          int pin = inputString.substring(11,firstSlashPosition).toInt();
          noTone(pin);
      }
      else if(opcode == "srld") { // Start Listen Digital Pin
          String pin = inputString.substring(11);
          digitalPinListening[pin.toInt()] = true;
          digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt(), INPUT);
      }
      else if(opcode == "spld") { // Stop Listen Digital Pin
          String pin = inputString.substring(11);
          digitalPinListening[pin.toInt()] = false;
          digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      }
      else if(opcode == "srla") { // Start Listen Analog Pin
          String pin = inputString.substring(11);
          pin.trim();
          analogPinListening[pin.toInt()] = true;
          analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt() + ANALOG_PIN_NUM_START, INPUT);
          //Serial.print("srla:");
          //Serial.println(pin.toInt());
      }
      else if(opcode == "spla") { // Stop Listen Analog Pin
          String pin = inputString.substring(11);
          analogPinListening[pin.toInt()] = false;
          analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      }
      else if(opcode == "cust") { // Custom Message
        int firstSlashPosition = inputString.indexOf('/', 11);
        String msgCode = inputString.substring(11, firstSlashPosition);
        // Change analogic frequency
        if (msgCode == "afreq") {
          int secondSlashPosition = inputString.indexOf('/', firstSlashPosition + 1);
          int pin = inputString.substring(firstSlashPosition + 1, secondSlashPosition).toInt();
          int frequency = inputString.substring(secondSlashPosition + 1).toInt();
          analogPinListenedFrequency[pin] = frequency;
          analogPinListenedLastRead[pin] = 0;
          /*Serial.print("Change frequency for pin A");
          Serial.print(pin);
          Serial.print(" = ");
          Serial.print(1000 / frequency);
          Serial.print("Hz (delay: ");
          Serial.print(frequency);
          Serial.println(" ms)");
          Serial.flush();*/
        }
        // Change analogic frequency
        else if (msgCode == "atrhl") {
          int secondSlashPosition = inputString.indexOf('/', firstSlashPosition + 1);
          int pin = inputString.substring(firstSlashPosition + 1, secondSlashPosition).toInt();
          int threshold = inputString.substring(secondSlashPosition + 1).toInt();
          analogPinListenedThreshold[pin] = threshold;
          /*Serial.print("Change threshold for pin A");
          Serial.print(pin);
          Serial.print(" = ");
          Serial.println(threshold);
          Serial.flush();*/
        }
        else 
          msgRecognized = false;
      }
      else {
        msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
      }
      
      // Prepare reply message if caller supply a message id
      int idPosition = inputString.indexOf("?id=");
      if(idPosition != -1) {
        String id = inputString.substring(idPosition + 4);
        // print the reply
        Serial.print("alp://rply/");
        if(msgRecognized) { // this sketch doesn't know other messages in this case command is ko (not ok)
          Serial.print("ok?id=");
        } else {
          Serial.print("ko?id=");
        }
        Serial.print(id);
        Serial.print('\n'); // End of Message
        Serial.flush();
      }
    }
  }
  
  // Send listen messages
  int index = 0;
  for (index = 0; index < DIGITAL_PIN_COUNT; index++) {
    if(digitalPinListening[index] == true) {
      int value = digitalRead(index);
      if(value != digitalPinListenedValue[index]) {
        digitalPinListenedValue[index] = value;
        Serial.print("alp://dred/");
        Serial.print(index);
        Serial.print("/");
        Serial.print(value);
        Serial.print('\n'); // End of Message
        Serial.flush();
      }
    }
  }
  long now = millis();
  for (index = 0; index < ANALOG_PIN_COUNT; index++) {
    // Is listening ?
    if (analogPinListening[index] != true) continue;
    // Frequency
    if (analogPinListenedFrequency[index] != 0) {
      if (now < analogPinListenedLastRead[index]) continue; // Wait
      analogPinListenedLastRead[index] = millis() + analogPinListenedFrequency[index];
    }
    // Read analogic value
    int value = highPrecisionAnalogRead(index + ANALOG_PIN_NUM_START);
    if (abs(value - analogPinListenedValue[index]) > analogPinListenedThreshold[index]) {
      analogPinListenedValue[index] = value;
      Serial.print("alp://ared/");
      Serial.print(index);
      Serial.print("/");
      Serial.print(value);
      Serial.print('\n');
      Serial.flush();
    }
  }
}

// Reads 4 times and computes the average value
int highPrecisionAnalogRead(int pin) {
  double value1 = analogRead(pin);
  double value2 = analogRead(pin);
  double value3 = analogRead(pin);
  double value4 = analogRead(pin);
  
  return (int)((value1 + value2 + value3 + value4) / 4.0);
}

