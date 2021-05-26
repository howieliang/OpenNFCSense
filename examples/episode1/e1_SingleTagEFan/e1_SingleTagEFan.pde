/*========================================================================== 
 //  OpenNFCSense4P (v2) - Open NFCSense API for Processing Language
 //  Copyright (C) 2021 Dr. Rong-Hao Liang
 //    This program is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    This program is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 //==========================================================================*/
// e1_SingleTagRotation.pde:
// This software works with a microcontroller running Arduino code and connected to an RC522 NFC/RFID Reader
// OpenNFCSense API Github repository: https://github.com/howieliang/OpenNFCSense
// NFCSense Project website: https://ronghaoliang.page/NFCSense/

import processing.serial.*;
import OpenNFCSense4P.*;
Serial port;
OpenNFCSense4P nfcs;

String lastString = "";
float lastFreq = 0;
long timer = 0;
float[] lastFreqArray = new float[9];

float[] pushValue(float[] array, float v){
  float[] _array = new float[array.length];
  arrayCopy(array,_array);
  for(int i = 0; i < _array.length-1; i++){
    _array[i+1] = array[i]; 
  }
  _array[0] = v;
  return _array;
}
float medianFilter(float[] array){
  float[] _array = new float[array.length];
  arrayCopy(array,_array);
  _array=sort(_array);
  println(_array);
  return _array[floor(_array.length/2)];
}

void setup() {
  size(800, 600, P2D);              //Start a 800x600 px canvas (using P2D renderer)
  nfcs = new OpenNFCSense4P(this, "tagProfile.csv"); //Initialize OpenNFCSense with the tag profiles (*.csv) in /data
  //nfcs = new OpenNFCSense4P(this, "demo.csv", 250, 500, 2000); 
  ////set frame rate to 250fps, TTL timer1 to 500ms, and TTL timer2 to 2000ms  
  initSerial();                     //Initialize the serial port
  /*== Put your codes below ==*/
  timer = millis()-7000;
  /*== Put your codes above ==*/
}

void draw() {
  nfcs.updateNFCBits();                   //update the features of current bit
  /*== Put your codes below ==*/
  ArrayList<NFCBit> nbitList = nfcs.getNFCBits();
  if (nbitList.size()>0) {
    String s = nbitList.get(0).getName();
    if (lastString != s) {
      lastString = s;
    }
    if (lastString.equals("E-fan")){
      lastFreqArray=pushValue(lastFreqArray, nbitList.get(0).getFrequency());
      lastFreq = medianFilter(lastFreqArray);
    }
    timer = millis();
  }
  println(lastString);
  /*== Put your codes above ==*/
  background(255);                  //Refresh the screen
  nfcs.drawMotionModeRecords(50, 2*height/3, 2*width/3, height/3-50); //draw the motion mode record of last second (x,y,width,height)

  drawLastBit(50, 100);                 //print the extra information of the last NFCBit in the console
  /*== Put your codes below ==*/
  if (millis()-timer<1000) {
    if (lastString.equals("E-fan")) {
      if (lastFreq > 52) {
        drawString("Full Speed: 15-20 mins left", 50, height/4+100);
      } else if (lastFreq > 40 && lastFreq <= 52) {
        drawString("Mid Speed: 20-25 mins left", 50, height/4+100);
      } else if (lastFreq > 25 && lastFreq <= 40) {
        drawString("Low Speed: 25-30 mins left", 50, height/4+100);
      } else if (lastFreq <= 25 && lastFreq > 2) {
        drawString("...", 50, height/4+100);
      } else {
        drawString("Off.", 50, height/4+100);
      }
    }
  } else {
    if (lastString == "E-fan") {
      drawString("Off.", 50, height/4+100);
    }
  }
  /*== Put your codes above ==*/
  //drawRecentBits(50, height/4+100); //draw the basic information of the recent NFCBits (within Timer2)
  drawInfo(width, height);          //draw the version, read rate, and TTL timer info
  //printLastBit();                 //print the extra information of the last NFCBit in the console
  //printCurrentTag();                //print the current tag read by the reader
}

/*Initialize the serial port*/
void initSerial() {             
  for (int i = 0; i < Serial.list().length; i++) println("[", i, "]:", Serial.list()[i]); //print the serial port
  port = new Serial(this, Serial.list()[Serial.list().length-1], 115200); //for Mac and Linux 
  // port = new Serial(this, Serial.list()[0], 115200); // for PC
  port.bufferUntil('\n');           // arduino ends each data packet with a carriage return 
  port.clear();                     // flush the Serial buffer
}

/*draw a formatted String*/
void drawString(String s, int x, int y) { //print the latest tag appearance
  pushStyle();
  fill(0);
  textSize(48);
  text(s, x, y);
  popStyle();
}

/*draw the basic information of the last NFCBit*/
void drawLastBit(int x, int y) { //print the latest tag appearance
  ArrayList<NFCBit> nbitList = nfcs.getNFCBits();       // get all of the recent NFCBits
  String objInfo = "Current object: ", motionInfo = "";
  String tagID = "";
  if (nbitList.size()>0) {
    NFCBit nb = nbitList.get(0);                // get the latest NFCBit  
    tagID = "["+nfcs.getIDTable().get(0)+"]";   // get the ID string of the latest tag
    objInfo += nb.getName();
    if (nb.getMode()!=NFCBit.NA) {              // if the feature is ready (mode!=NA=0)
      if (nb.isFrequency()) {          // if the NFCBit is a frequency bit
        motionInfo += nb.getModeString()+" at "+nf(nb.getFrequency(), 0, 2)+" Hz"; //show the motion mode and the motion frequency
      } else {
        motionInfo += nb.getModeString()+" at "+nf(nb.getSpeed(), 0, 2)+" km/h"; //show the motion mode and the motion speed
      }
    }
  }
  pushStyle();
  fill(0);
  textSize(48);
  text(objInfo, x, y);
  textSize(32);
  text(motionInfo, x, y+48);
  textSize(16);
  text(tagID, x, y+48+32);
  popStyle();
}

/*draw the basic information of the recent NFCBits (within timer 2)*/
void drawRecentBits(int x, int y) {
  ArrayList<NFCBit> nbitList = nfcs.getNFCBits(); // get all of the recent NFCBits
  ArrayList<String> IDList = nfcs.getIDTable();   // get the ID string of the recent NFCBits
  String tagName = "";
  String tagID = "";
  //for (int i = 0; i < nbitList.size(); i++) { // to reverse the order
  for (int i = nbitList.size()-1; i >=0; i--) {
    NFCBit nb = nbitList.get(i);
    tagName += nb.getName();
    tagID += "["+IDList.get(i)+"]";
    if (i>0) { 
      tagName += ", ";
      tagID += ", ";
    }
  }
  pushStyle();
  fill(0);
  textSize(48);
  text("Recent objects: ", x, y);
  textSize(32);
  text(tagName, x, y+48);
  textSize(16);
  text(tagID, x, y+48+32);
  popStyle();
}

/*draw the version, read rate, and TTL timer info*/
void drawInfo(int x, int y) { 
  String info = "[Open NFCSense] ver. "+OpenNFCSense4P.version()+"\n"; //get the current library version
  info += "Read rate: "+nf(nfcs.getReadRate(), 0, 0)+" reads/sec\n"; // get the current read rate
  info += "TTL Timer1: "+nf(nfcs.getTimer1(), 0, 0)+" ms\n";         // get the current TTL timer1
  info += "TTL Timer2: "+nf(nfcs.getTimer2(), 0, 0)+" ms";           // get the current TTL timer2
  //set the above parameters in the constructor as indicated in setup();
  pushStyle();
  fill(100);
  textSize(12);
  textAlign(RIGHT, BOTTOM);
  text(info, x-5, y-5);
  popStyle();
}

/*print the extra information of the recent NFCBits in the console*/
void printRecentBits() {
  ArrayList<NFCBit> nbitList = nfcs.getNFCBits(); // get all of the recent NFCBits
  for (int i = 0; i < nbitList.size(); i++) {
    NFCBit nb = nbitList.get(i);                  // get the latest NFCBit at index i
    String tagID = "["+nfcs.getIDTable().get(i)+"]"; //get the ID string of the latest tag
    print("[", i, "]", tagID, nb.getName(), ":", nb.getModeString(), "(mode= ", nb.getMode(), ") ");
    //print the name of tag, the ID, the mode of motion depending on the motion type (mode=0: not ready), determined by the m and n in the algorithm.
    print(nb.getTokenTypeString(), nb.getMotionTypeString(), "|");
    //print the type of token (e.g., z<z*, z>z*, theta>theta*, d_gap>d_gap*)
    //the type of motion (e.g., linear translation, rotation, shm, compound+motion)
    println("V=", nf(nb.getSpeed(), 0, 2), "km/h; f=", nf(nb.getFrequency(), 0, 2), "Hz");
    //the features (speed and frequency)
  }
  println("===");
}

/*print the extra information of the last NFCBit in the console*/
void printLastBit() {
  ArrayList<NFCBit> nbitList = nfcs.getNFCBits(); // get all of the recent NFCBits
  if (nbitList.size()>0) {
    NFCBit nb = nbitList.get(0);                  // get the latest NFCBit 
    String tagID = "["+nfcs.getIDTable().get(0)+"]"; //get the ID string of the latest tag
    print(nb.getName(), tagID, ":", nb.getModeString(), "(mode= ", nb.getMode(), ") "); 
    //print the name of tag, the ID, the mode of motion depending on the motion type (mode=0: not ready), determined by the m and n in the algorithm.  
    print(nb.getTokenTypeString(), nb.getMotionTypeString(), "|");
    //print the type of token (e.g., z<z*, z>z*, theta>theta*, d_gap>d_gap*)
    //the type of motion (e.g., linear translation, rotation, shm, compound, compound+motion)
    println("V=", nf(nb.getSpeed(), 0, 2), "km/h; f=", nf(nb.getFrequency(), 0, 2), "Hz");
    //the features (speed and frequency)
  }
  println("===");
}

/*The serial event handler processes the data from any NFC reader in the String format of 
 // "A[Byte_0]\n, B[Byte_1]\n, C[Byte_2]\n, or D[Byte_3]\n", 
 //where every byte is an unsigned integer ranged between [0-256].  
 //When a tag is present: Byte_i=[0-255]; Otherwise, when a tag is absent: [256].
 //================*/

void serialEvent(Serial port) {   
  String inData = port.readStringUntil('\n');  // read the serial string until seeing a carriage return
  if (inData.charAt(0) >= 'A' && inData.charAt(0) <= 'D') {
    int i = inData.charAt(0)-'A';
    int v = int(trim(inData.substring(1)));
    nfcs.rfid[i] = (v>255?-1:v);
    if (i==3) nfcs.checkTagID();                // process the tag ID when a sequence is collected completely.
  }
  return;
}

//print the current tag read by the reader
void printCurrentTag() {
  if (nfcs.rfid[0]<0) println("No tag");
  else println(nfcs.rfid[0], ",", nfcs.rfid[1], ",", nfcs.rfid[2], ",", nfcs.rfid[3]);
}
