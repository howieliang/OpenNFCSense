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
String tempString = "";
float lastFreq = 0;
long timer = 0;
boolean bEntered = true;

String[] QStrings = {"1A2B", "2B3C", "3C1A"};
String[] AStrings = {"1A2B", "2B3C", "3C1A"};
int qIndex = 0;
boolean bCorrect = false;

PImage[] img  = new PImage[3];
int pw;

void setup() {
  size(800, 600, P2D);              //Start a 800x600 px canvas (using P2D renderer)
  //nfcs = new OpenNFCSense4P(this, "tagProfile.csv"); //Initialize OpenNFCSense with the tag profiles (*.csv) in /data
  nfcs = new OpenNFCSense4P(this, "tagProfile.csv", 300, 300, 300); 
  ////set frame rate to 250fps, TTL timer1 to 500ms, and TTL timer2 to 2000ms  
  initSerial();                     //Initialize the serial port
  /*== Put your codes below ==*/
  img[0] = loadImage("apple.png");
  img[1] = loadImage("banana.png");
  img[2] = loadImage("carrot.png");
  pw = height/8;
  for (int i = 0; i < 3; i++) {
    img[i].resize(pw, pw);
  }
  /*== Put your codes above ==*/
}

void draw() {
  nfcs.updateNFCBits();                   //update the features of current bit
  /*== Put your codes below ==*/
  ArrayList<NFCBit> nbitList = nfcs.getNFCBits();
  if (nbitList.size()>0) {
    lastString = "";
    tempString = "";
    for (int i = nbitList.size()-1; i >=0; i--) {
      NFCBit nb = nbitList.get(i);
      if (nb.getName().equals("Apple")) tempString += "A";
      else if (nb.getName().equals("Banana")) tempString += "B";
      else if (nb.getName().equals("Carrot")) tempString += "C";
      else tempString += nb.getName();
    }
    timer = millis();
    bEntered = false;
  }
  /*== Put your codes above ==*/
  background(255);                  //Refresh the screen
  nfcs.drawMotionModeRecords(50, 2*height/3, 2*width/3, height/3-50); //draw the motion mode record of last second (x,y,width,height)
  /*== Put your codes below ==*/
  if (millis()-timer>100 && bEntered == false) {
    lastString = tempString;
    bEntered = true;
  }
  if (millis()-timer>2000) {
    if (!lastString.equals("")) {
      if (bCorrect) {
        ++qIndex;
        qIndex %= QStrings.length;
        lastString = "";
      } else {
        lastString = "";
      }
    }
  }
  if (lastString.equals("")) {
    drawString("Q:", 50, 40, color(0));
    drawString("A:", 50, 40+pw, color(0));
  } else {
    if (lastString.equals(AStrings[qIndex])) bCorrect = true;
    else bCorrect = false;
    drawString("Q:", 50, 40, color(0));
    if (bCorrect) drawString("A:", 50, 40+pw, color(22, 160, 133));
    else drawString("A:", 50, 40+pw, color(231, 76, 60));
  }
  int nq1 = (int)QStrings[qIndex].charAt(0)-'0';
  int oq1 = (int)QStrings[qIndex].charAt(1)-'A';
  int nq2 = (int)QStrings[qIndex].charAt(2)-'0';
  int oq2 = (int)QStrings[qIndex].charAt(3)-'A';
  for (int i = 0; i < nq1; i++) {
    image(img[oq1], 120+i*pw, 40);
  }
  for (int i = nq1; i < nq1+nq2; i++) {
    image(img[oq2], 120+i*pw, 40);
  }
  int n1 = 0; 
  int o1 = 0;
  int n2 = 0;
  int o2 = 0;
  if(lastString.length()>=2){
    n1 = lastString.charAt(0)-'0'; 
    o1 = lastString.charAt(1)-'A';
  }
  if(lastString.length()>=4){
    n2 = lastString.charAt(2)-'0'; 
    o2 = lastString.charAt(3)-'A';
  }
  if (n1>0 && o1>=0) { 
    for (int i = 0; i < n1; i++) {
      image(img[o1], 120+i*pw, 40+pw);
    }
  }
  if (n2>0 && o2>=0) {
    for (int i = n1; i < n1+n2; i++) {
      image(img[o2], 120+i*pw, 40+pw);
    }
  }
  /*== Put your codes above ==*/
  drawRecentBits(50, height/4+100); //draw the basic information of the recent NFCBits (within Timer2)
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
void drawString(String s, int x, int y, color c) { //print the latest tag appearance
  pushStyle();
  textAlign(LEFT,TOP);
  fill(c);
  textSize(48);
  text(s, x, y);
  popStyle();
}

void drawSpeedmeter(float TacoValue, float _x, float _y, float _w, float _h, float _r) {
  float blight = 90;
  float r = _r;
  pushStyle();
  colorMode(HSB, 140);
  float TacoValueMap = map(TacoValue, 0, 30, -40, 220);
  float theta = radians(TacoValueMap);
  float x;
  float y;
  x = -r*cos(theta);
  y = -r*sin(theta);

  fill(30, 0, blight);
  textAlign(CENTER);
  textSize(50);

  pushMatrix();
  translate(_x, _y);

  pushMatrix();
  translate(_w/2, _h/2);
  rotate(-70 * PI / 180);
  strokeCap(SQUARE);
  strokeWeight(10);
  stroke(55, 99, 30);
  for (int i = 0; i < 7; i++) {
    rotate(40 * PI / 180);
    line(-r-10, 0, -r+33, 0);
  }
  popMatrix();

  pushMatrix();
  translate(_w/2, _h/2);
  rotate(-70 * PI / 180);
  strokeCap(ROUND);
  strokeWeight(8);
  stroke(55, 80, blight);
  for (int i = 0; i < 7; i++) {
    rotate(40 * PI / 180);
    line(-r-8, 0, -r+32, 0);
  }
  popMatrix();

  pushMatrix();
  translate(_w/2, _h/2);
  rotate(-30 * PI / 180);
  strokeCap(ROUND);
  strokeWeight(4);
  stroke(55, 80, blight);
  for (int i = 0; i < 24; i++) {
    rotate(10 * PI / 180);
    line(-r-8, 0, -r+10, 0);
  }
  popMatrix();

  pushMatrix();
  translate(_w/2, _h/2);
  rotate(-30 * PI / 180);
  strokeCap(ROUND);
  strokeWeight(4);
  stroke(55, 80, blight);
  for (int i = 0; i < 48; i++) {
    rotate(5 * PI / 180);
    line(-r-8, 0, -r, 0);
  }
  popMatrix();

  pushMatrix();
  translate(_w/2, _h/2);
  noFill();
  stroke(0, 0, 50);
  strokeWeight(15);
  stroke(0, 99, 30);
  strokeWeight(12);
  strokeCap(ROUND);
  line(0, 0, x, y);
  stroke(0, 99, blight);
  strokeWeight(8);
  line(0, 0, x, y);
  stroke(0, 0, blight);
  strokeWeight(6);
  line(x/10, y/10, x/5, y/5);
  popMatrix();

  stroke(0, 0, 30);
  strokeWeight(3);
  fill(0, 0, blight);
  ellipse(_w/2, _h/2, 44, 44);
  popMatrix();
  popStyle();
  println(TacoValueMap);
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
