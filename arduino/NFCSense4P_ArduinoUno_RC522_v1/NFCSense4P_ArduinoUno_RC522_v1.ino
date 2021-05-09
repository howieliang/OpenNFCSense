//*********************************************
// Arduino Codes for NFCSense,  which is based on
// - NXP MFRC522 NFC/RFID Reader (e.g., RFID RC522)
// - RFID.cpp Library (created by Dr. Leong and Miguel Balboa)
// for further information please check 
// our website: https://ronghaoliang.page/NFCSense
// or contact Dr. Rong-Hao Liang (TU Eindhoven) via r.liang@tue.nl
//*********************************************
//Copyright <2021> Dr. Rong-Hao Liang
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
//INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
//IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#include <SPI.h> 
#include "RFID.h"
#define SS_PIN 10
#define RST_PIN 9
#define UID_BYTEBUM 4 //The 4-Byte UID

#define REFRESH_RATE 300 //unit: Hz
//Recommended parameters: 300 for Arduino Uno and Leonardo, 250 for ESP32, 200 for Teensy 3.2

RFID rfid(SS_PIN, RST_PIN);
int UID[UID_BYTEBUM]; //UID: Unique IDentifier
char dataID[UID_BYTEBUM] = {'A','B','C','D'}; //data label
long timer = micros(); //timer

void setup()
{
  Serial.begin(115200);
  SPI.begin();
  rfid.init();
  for (int i = 0 ; i < UID_BYTEBUM ; i++)  UID[i] = -1;
} 

void loop()
{
  if (micros() - timer >= (1000000/REFRESH_RATE) ) { //Timer: send sensor data in every 10ms
    timer = micros();
    if (rfid.isCard()) {  // A tag is detected
      if (rfid.readCardSerial()) { // Read the tag's ID
        for (int i = 0 ; i < UID_BYTEBUM ; i++)  UID[i] = rfid.serNum[i];
      }
    } 
    else { // No tag is detected.
      for (int i = 0 ; i < UID_BYTEBUM ; i++)  UID[i] = 256; //256: absence
    }
#if PRINT_IN_SERIAL_PLOTTER
    printInSerialPlotter(UID);
#else
    for (int i = 0 ; i < UID_BYTEBUM ; i++) {
      sendDataToProcessing(dataID[i], UID[i]);
    }
#endif
    rfid.halt(); //Halt the rfid reader
  }
}

void printInSerialPlotter(int UID[]) { //println(uid[0],uid[1],uid[2],uid[3],uid[4]);
  for(int i=0; i< UID_BYTEBUM; i++){
    Serial.print(UID[i]);
    (i<(UID_BYTEBUM-1) ? Serial.print(','): Serial.println());
  }
}

void sendDataToProcessing(char symbol, int data) {
  Serial.print(symbol);  // symbol prefix of data type
  Serial.println(data);  // the integer data with a carriage return
}
