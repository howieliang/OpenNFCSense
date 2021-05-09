/*========================================================================== 
 //  OpenNFCSense4P (v1) - Open NFCSense API for Processing Language
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
// This software works with a microcontroller connected to an RC522 NFC/RFID Reader 
// and run the Arduino code
// Github repository: https://github.com/howieliang/NFCSense
// Project website: https://ronghaoliang.page/NFCSense/

package OpenNFCSense4P;

import java.util.ArrayList;

import processing.core.*;
import processing.data.*;

/**
 * OpenNFCSense
 *
 * @example Hello
 */

public class OpenNFCSense4P implements PConstants {

	// pa is a reference to the parent sketch
	PApplet pa;
	public final static String VERSION = "##library.prettyVersion##";
	
	final int IDBitNum = 4;
	final int MotionTypeNum = 8;
	
	float SAMPLE_RATE = 300.0f; // reads per second
	float TTL1_THLD = 300.0f;  //ttl_timer 1 threshold
	float TTL2_THLD = 1000.0f; //ttl_timer 2 threshold
	
	boolean DEBUG_ID = false;
	boolean DEBUG_TIMER = false;

	int idNum = 0;
	ArrayList<Long> id_db = new ArrayList<Long>();

	
	public int[] rfid = new int[IDBitNum];
	int[] lastRFID = new int[IDBitNum];

	int[] ibiCounter, tOnCounter, ibiLastEventTS;
	float[] lastIBI;
	float[][] idArray, mTypeArray, idSCntArray, tOnCntrArray;

	public float[][] RFIDHist;
	int streamSize = (int) SAMPLE_RATE;
	float[] modeArray = new float[streamSize];
	float[] eventArray = new float[streamSize];
	ArrayList<Integer> lastIDList;

	// GLOBAL Variables
	public int lastRead = -1; // GLOBAL variable of last read.
	public int lastID = -1; // GLOBAL. Only for control.
	public int lastIDIndex = -1;
	public String IDModeString = "";
	public String lastIDString = "";
	public int ts = 0;
	public boolean profileReady = false;
	public int colNum = 3;
	public int gH, gW;
	public String[] fontList;
	public int fontSize = 24;
	public int tSize = 64;
	public PFont font;

	// TTL timers
	int last_t1 = 0;
	int last_t2 = 0;
	int ttl1 = 0;
	int ttl2 = 0;
	

	// Container
	ArrayList<NFCBit> nbits;

	/**
	 * OpenNFCSense Constructor
	 * 
	 * @param theParent the parent PApplet
	 */
	public OpenNFCSense4P(PApplet theParent, String fileName) {
		pa = theParent;
		initNFCBits(fileName);
		welcome();
	}
	
	public OpenNFCSense4P(PApplet theParent, String fileName, float sample_rate) {
		pa = theParent;
		initNFCBits(fileName);
		SAMPLE_RATE = sample_rate;
		welcome();
	}
	
	public OpenNFCSense4P(PApplet theParent, String fileName, float sample_rate, int timer1, int timer2) {
		pa = theParent;
		initNFCBits(fileName);
		SAMPLE_RATE = sample_rate;
		TTL1_THLD = timer1;  //ttl_timer 1 threshold
		TTL2_THLD = timer2; //ttl_timer 2 threshold
		welcome();
	}

	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	/**
	 * return the version of the Library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
	
	public void pre() {
//		updateNFCBits();
	}

	// ##Init
	void initNFCBits(String fileName) {
		Table csvData = pa.loadTable(fileName, "header");
		nbits = new ArrayList();
		if (csvData != null) {
			int n = csvData.getRowCount();
			// ArrayList<Integer> uids = new ArrayList();
			for (int r = 0; r < n; r++) {
				TableRow newRow = csvData.getRow(r);
				int uid0 = newRow.getInt("UID0");
				int uid1 = newRow.getInt("UID1");
				int uid2 = newRow.getInt("UID2");
				int uid3 = newRow.getInt("UID3");
				String tagName = newRow.getString("Label");
				int tType = newRow.getInt("Ttype");
				int mType = newRow.getInt("Mtype");
				float lon1 = newRow.getFloat("L1");
				float lon2 = newRow.getFloat("L2");
				int[] _rfid = { uid0, uid1, uid2, uid3, 0 };
				id_db.add(IDtoLong(_rfid));

				if (tType == NFCBit.BELOW_Z_STAR && mType == NFCBit.L_MOTION) {
					nbits.add(new NFCBitB(new float[] { lon1, lon2 }, new String[] { tagName }));
				}
				if (tType == NFCBit.ABOVE_THETA_STAR && mType == NFCBit.L_MOTION) {
					nbits.add(new NFCBitT(new float[] { lon1, lon2 }, new String[] { tagName }));
				}
				if (tType == NFCBit.ABOVE_Z_STAR && mType == NFCBit.L_MOTION) {
					nbits.add(new NFCBitAL(new float[] { lon1, lon2 }, new String[] { tagName }));
				}
				if (tType == NFCBit.ABOVE_Z_STAR && mType == NFCBit.ROTATION) {
					nbits.add(new NFCBitAR(new float[] { lon1, lon2 }, new String[] { tagName }));
				}
				if (tType == NFCBit.ABOVE_Z_STAR && mType == NFCBit.SHM) {
					nbits.add(new NFCBitAS(new float[] { lon1, lon2 }, new String[] { tagName }));
				}
				if (mType == NFCBit.COMPOUND_R1 || mType == NFCBit.COMPOUND_R2 || mType == NFCBit.COMPOUND_R3) {
					nbits.add(new NFCBitCR(new float[] { lon1, lon2 }, new String[] { tagName }, mType));
				}
				if (mType == NFCBit.COMPOUND_L1 || mType == NFCBit.COMPOUND_L2) {
					nbits.add(new NFCBitCL(new float[] { lon1, lon2 }, new String[] { tagName }, mType));
				}
			}
		}

		initParam();
	}

	void initParam() {
		fontList = PFont.list();
		font = pa.createFont("Lucida Sans Regular", 72);
		pa.textFont(font);
		gH = pa.height;
		gW = pa.width;
		last_t1 = pa.millis();
		last_t2 = pa.millis();

		idNum = id_db.size(); // new
		lastIDList = new ArrayList<Integer>();

		ibiCounter = new int[idNum];
		tOnCounter = new int[idNum];
		ibiLastEventTS = new int[idNum];
		lastIBI = new float[idNum];

		rfid = new int[IDBitNum];
		lastRFID = new int[IDBitNum];
		idSCntArray = new float[idNum][streamSize]; // history data to show
		tOnCntrArray = new float[idNum][streamSize]; // history data to show

		// RFIDHist = new float[IDBitNum][streamSize]; //history data to show
		idArray = new float[idNum][streamSize];
		mTypeArray = new float[MotionTypeNum][streamSize];
		modeArray = new float[streamSize]; // To show activated or not
		eventArray = new float[streamSize]; // To show activated or not

		for (int i = 0; i < idNum; i++) {
			for (int j = 0; j < streamSize; j++) {
				idArray[i][j] = -1; // history data to show
			}
			tOnCounter[i] = 0;
			ibiLastEventTS[i] = 0;
		}
		
		for (int i = 0; i < MotionTypeNum; i++) {
			for (int j = 0; j < streamSize; j++) {
				mTypeArray[i][j] = -1; // history data to show
			}
		}

		for (int i = 0; i < streamSize; i++) {
			modeArray[i] = -1;
			eventArray[i] = -1;
		}

		for (int i = 0; i < IDBitNum; i++) {
			rfid[i] = -1;
			lastRFID[i] = -1;
		}

		lastIDString = "";
		lastIDIndex = -1;
		ts = 0;
	}

	// ##Draw

	void drawIDInfo(String str) {
		if (str == "")
			showInfo("Waiting for the first tag...", gW / 2, 20, 20);
		else
			showInfo("Last Tag: " + str + " is recognized as:", gW / 2, 20, 20);
	}

	void drawLMotionSignals(int n) {
		lineGraph2(tOnCntrArray[n], 300, 0, 0, 2 * gH / colNum, gW, gH / (colNum * 2), n); // history of signal
		barGraph(modeArray, 0, 3 * gH / colNum, gW, gH / (colNum * 2));
	}

	void drawRotationSignals(int n) {
		lineGraph2(idSCntArray[n], 20, 0, 0, 2 * gH / colNum, gW, gH / colNum, n); // history of signal
	}

	void drawSHMSignals(int n) {
		lineGraph2(tOnCntrArray[n], 300, 0, 0, 2 * gH / colNum, gW, gH / colNum, n); // history of signal
	}

	void drawIDStream(int n) {
		barGraph(idArray[n], 0, 2 * gH / colNum - n * gH / (colNum * idNum), gW, gH / (colNum * idNum), n); // history
																											// of signal
	}

	public void drawResults() {
		pa.pushStyle();
		drawIDInfo(lastIDString);
		for (int n = 0; n < idNum; n++) {
			NFCBit nb = nbits.get(n);
			int mType = nb.getMotionType();
			drawIDStream(n);
			if (lastIDIndex == n && IDModeString.length() > 0) {
				switch (mType) {
					case NFCBit.L_MOTION:
						drawLMotionSignals(n);
						if (profileReady) {
							nb.getInfo();
						}
						break;
					case NFCBit.ROTATION:
						drawRotationSignals(n);
						nb.getInfo();
						break;
					case NFCBit.SHM:
						drawSHMSignals(n);
						nb.getInfo();
						break;
					default:
						drawRotationSignals(n);
						nb.getInfo();
						break;
				}
			}
		}
		pa.noFill();
		pa.stroke(0);
		for (int i = 0; i < colNum; i++)
		pa.line(0, i * pa.height / colNum, pa.width, i * pa.height / colNum);
			pa.popStyle();
	}

	void showInfo(String s, float x, float y, float fs) {
		pa.pushStyle();
		pa.textAlign(CENTER, TOP);
		pa.fill(0);
		pa.textSize(fs);
		pa.text(s, x, y);
		pa.popStyle();
	}

	// Draw a bar graph to visualize the modeArray
	// barGraph(float[] data, float x, float y, float width, float height)
	void barGraph(float[] data, float _x, float _y, float _w, float _h) {
		int colors[] = { pa.color(155, 89, 182), pa.color(63, 195, 128), pa.color(214, 69, 65), pa.color(82, 179, 217),
				pa.color(52, 73, 94), pa.color(242, 121, 53), pa.color(0, 121, 53), pa.color(128, 128, 0), pa.color(52, 0, 128),
				pa.color(128, 52, 0) };
				pa.pushStyle();
				pa.noStroke();
		float delta = _w / data.length;
		for (int p = 0; p < data.length; p++) {
			float i = data[p];
			int cIndex = (int) i % 10;// min(, colors.length-1);
			if (i < 0)
			pa.fill(255);
			else {
				if (i >= 10) {
					pa.fill(colors[cIndex]);
				} else {
					pa.fill(colors[cIndex], 52);
				}
			}
			float h = PApplet.map(0, -1, 0, 0, _h);
			pa.rect(_x, _y - h, delta, h);
			_x = _x + delta;
		}
		pa.popStyle();
	}

	void barGraph(float[] data, float _x, float _y, float _w, float _h, int ci) {
		int colors[] = { pa.color(155, 89, 182), pa.color(63, 195, 128), pa.color(214, 69, 65), pa.color(82, 179, 217),
				pa.color(52, 73, 94), pa.color(242, 121, 53), pa.color(0, 121, 53), pa.color(128, 128, 0), pa.color(52, 0, 128),
				pa.color(128, 52, 0), pa.color(0) };
				pa.pushStyle();
				pa.noStroke();
		float delta = _w / data.length;
		for (int p = 1; p<data.length; p++) {
			float i = data[p];
			float p_i = data[p-1];
			if (i < 0)
			pa.fill(255);
			else {
				if (p_i < 0)
				pa.fill(0);
				else
				pa.fill(colors[ci], 100);
			}
			float h = PApplet.map(0, -1, 0, 0, _h);
			pa.rect(_x, _y, delta, h);
			_x = _x + delta;
		}
		pa.popStyle();
	}

	void lineGraph2(float[] data, float _l, float _u, float _x, float _y, float _w, float _h, int _index) {
		int colors[] = { pa.color(155, 89, 182), pa.color(63, 195, 128), pa.color(214, 69, 65), pa.color(82, 179, 217),
				pa.color(52, 73, 94), pa.color(242, 121, 53), pa.color(0, 121, 53), pa.color(128, 128, 0), pa.color(52, 0, 128),
				pa.color(128, 52, 0), pa.color(0) };
		int index = PApplet.min(PApplet.max(_index, 0), colors.length);
		pa.pushStyle();
		float delta = _w / (data.length - 1);
		pa.beginShape();
		pa.noFill();
		pa.stroke(colors[index]);
		pa.strokeWeight(3);
		for (float i : data) {
			float h = PApplet.map(i, _l, _u, 0, _h);
			pa.vertex(_x, _y + h);
			_x = _x + delta;
			pa.vertex(_x, _y + h);
		}
		pa.endShape();
		pa.popStyle();
	}

	// Draw a line graph to visualize the sensor stream
	// lineGraph(float[] data, float lowerbound, float upperbound, float x, float y,
	// float width, float height, int _index)
	void lineGraph(float[] data, float _l, float _u, float _x, float _y, float _w, float _h, int _index) {
		int colors[] = { pa.color(100, 0, 0), pa.color(0, 100, 0), pa.color(0, 0, 100), pa.color(100, 0, 100), pa.color(100), };
		int index = PApplet.min(PApplet.max(_index, 0), colors.length);
		pa.pushStyle();
		float delta = _w / (data.length - 1);
		pa.beginShape();
		pa.noFill();
		pa.stroke(colors[index]);
		pa.strokeWeight(1);
		for (float i : data) {
			float h = PApplet.map(i, _l, _u, 0, _h);
			pa.vertex(_x, _y + h);
			_x = _x + delta;
			pa.vertex(_x, _y + h);
		}
		pa.endShape();
		pa.popStyle();
	}

	float[] appendArray(float[] _array, float _val) {
		float[] array = _array;
		float[] tempArray = new float[_array.length];
		PApplet.arrayCopy(array, tempArray, tempArray.length);
		array[array.length - 1] = _val;
		PApplet.arrayCopy(tempArray, 1, array, 0, tempArray.length - 1);
		return array;
	}
	
	public void printRecentBits() { //print the recent tag sequence
		  ArrayList<NFCBit> nbitList = getNFCBits();
		  for (int i = 0; i < nbitList.size(); i++) {
		    NFCBit nb = nbitList.get(i);
		    PApplet.print("[", i, "]", nb.getName(), ":", nb.getModeString(), "(mode= ", nb.getMode(), ") ");
		    PApplet.print(nb.getTokenTypeString(), nb.getMotionTypeString(), "|");
		    PApplet.println("V=", PApplet.nf(nb.getSpeed(), 0, 2), "km/h; f=", PApplet.nf(nb.getFrequency(), 0, 2), "Hz");
		  }
		  PApplet.println("===");
		}
	
	public void printLastBit() { //print the latest tag appearance
		  ArrayList<NFCBit> nbitList = getNFCBits();
		  if (nbitList.size()>0) {
		    NFCBit nb = nbitList.get(0);
		    PApplet.print(nb.getName(), ":", nb.getModeString(), "(mode= ", nb.getMode(), ") ");
		    PApplet.print(nb.getTokenTypeString(), nb.getMotionTypeString(), "|");
		    PApplet.println("V=", PApplet.nf(nb.getSpeed(), 0, 2), "km/h; f=", PApplet.nf(nb.getFrequency(), 0, 2), "Hz");
		  }
		  PApplet.println("===");
		}
	
	// #NFCSense Algorithm

	public void updateNFCBits() {
		if (lastIDList.size() > 0) {
			int lastID = lastIDList.get(lastIDList.size() - 1);
			NFCBit nbit = nbits.get(lastID);
			int previousMType = -1;
			if (lastIDList.size() > 1)
				previousMType = nbits.get(lastIDList.get(lastIDList.size() - 2)).getMotionType();
			nbit.getFeatures(previousMType);
		}
	}
	
	public ArrayList<NFCBit> getNFCBits() {
		  ArrayList<NFCBit> nbList = new ArrayList<NFCBit>();
		  for (int i=lastIDList.size()-1; i>=0; i--) {
		    nbList.add(nbits.get(lastIDList.get(i)));
		  }
		  return nbList;
		}

	int searchID(ArrayList<Long> id_db, long id) {
		int index = -1;
		for (int i = 0; i < id_db.size(); i++) {
			if (id_db.get(i) == id) {
				index = i;
				break;
			}
		}
		return index;
	}

	long IDtoLong(int[] b) {
		return (long) b[0] * 256 * 256 * 256 + (long) b[1] * 256 * 256 + (long) b[2] * 256 + (long) b[3];
	}

	String decoder(long l) {
		return (l / (256 * 256 * 256)) % 256 + "," + (l / (256 * 256)) % 256 + "," + (l / (256)) % 256 + "," + l % 256;
	}
	
	public void drawMotionModeRecords(float x, float y, float gW, float gH) {
		String[] mTypeItems={"LIN", "ROT", "SHM", "CR1", "CR2", "CR3", "CL1", "CL2"};
		int colors[] = {
				pa.color(155, 89, 182), pa.color(63, 195, 128), pa.color(214, 69, 65), 
				pa.color(82, 179, 217), pa.color(52, 73, 94), pa.color(242, 121, 53), 
				pa.color(0, 121, 53), pa.color(128, 128, 0), pa.color(52, 0, 128), 
				pa.color(128, 52, 0), pa.color(0)
			  };
		for (int n=0; n<MotionTypeNum; n++) {
		    barGraph(mTypeArray[n], x, y + n * gH / (float)(MotionTypeNum), gW, gH / (float)(MotionTypeNum), n); // history
		  }
		pa.pushStyle();
		pa.stroke(0);
		pa.noFill();
		pa.rect(x,y,gW,gH);
		pa.textAlign(RIGHT,TOP);
		pa.textSize((float)(0.6* gH / (float)(MotionTypeNum)));
	  for (float i=1; i<10; i++) {
	    float dx = (float)(i*(float)gW/10.);
	    pa.stroke(100);
	    pa.noFill();
	    pa.line(x+dx,y,x+dx,y+gH);
	    pa.noStroke();
	    pa.fill(100);
	    pa.text(pa.nf((float)(1.0-i/10.),0,1)+"s", x+dx-5, y+5);
	  }
	  pa.textAlign(RIGHT,BOTTOM);
	  pa.text("motion mode history (last second)", x+gW-5, y-5);
	  
	  pa.noStroke();
	  pa.textAlign(RIGHT,TOP);
	  for (int n=0; n<MotionTypeNum; n++) {
		  pa.fill(colors[n]);
		  pa.text(mTypeItems[n], x+gW-5, y + n * gH / (float)(MotionTypeNum)); // history
	  }
	  pa.popStyle();
	}
	
	public ArrayList<String> getIDTable() {
		ArrayList<String> ids = new ArrayList<String>();
		for(int i = 0; i < lastIDList.size();i++) {
			ids.add(decoder(id_db.get(lastIDList.get(i))));
		}
		return ids;
	}
	
	public ArrayList<NFCBit> getNFCBitTable() {
		return nbits;
	}
	
	public void setReadRate(float v) {
		SAMPLE_RATE = v;
	}
	
	public void setTimer1(float v) {
		TTL1_THLD = v;
	}
	
	public void setTimer2(float v) {
		TTL2_THLD = v;
	}
	
	public float getReadRate() {
		return SAMPLE_RATE;
	}
	
	public float getTimer1() {
		return TTL1_THLD;
	}
	
	public float getTimer2() {
		return TTL2_THLD;
	}

	public void checkTagID() { // called in Serial so that it won't be limited by the 60fps refresh rate
		int id_read = searchID(id_db, IDtoLong(rfid));
		updateOnTimers(id_read);
		updateIntervals(id_read);
		updateMTypes(id_read);
		lastRead = id_read; // update lastRead after the interval is obtained.
		if (id_read >= 0) {
			if (id_read != lastIDIndex) {
				IDModeString = nbits.get(id_read).getStateStrArray()[0];
				for (int i = lastIDList.size() - 1; i >= 0; i--) {
					if (id_read == lastIDList.get(i))
						lastIDList.remove(i);
				}
				lastIDList.add(id_read);
				lastIDIndex = id_read;
			}
		}
		if (DEBUG_ID)
			PApplet.println(rfid[0], rfid[1], rfid[2], rfid[3]);
		++ts; //increase the timestep
	}

	void updateOnTimers(int id_read) { // t1, t2, and idOnCounters
		// ====ON TIMER BEGIN====//
		ttl1 = pa.millis() - last_t1;

		if (id_read >= 0) { // id_read>=0
			lastIDString = "[" + rfid[0] + "," + rfid[1] + "," + rfid[2] + "," + rfid[3] + "]";
			NFCBit nb = nbits.get(id_read);
			if (lastID != id_read) { // edge of different tags
				for (int n = modeArray.length - 1; n >= 0; n--)
					modeArray[n] = -1;
			}
			// Calc T_on begin
			if (lastRead != id_read) {
				nb.reset();
				appendArray(modeArray, idNum + id_read);
				tOnCounter[id_read] = 1; // for single-tag pattern and orientation
			} else { // lastRead == id_read
				appendArray(modeArray, id_read);
				++tOnCounter[id_read]; // for single-tag pattern and orientation
			}
			// Calc T_on end
			last_t2 = pa.millis(); // reset t2

			// Calc T_ibi begin
			if (lastID != id_read) { // was a different tag
				if (ibiCounter[id_read] == 0) { //
					if (ibiLastEventTS[id_read] != 0)
						lastIBI[id_read] = ts - ibiLastEventTS[id_read];
					ibiLastEventTS[id_read] = ts;
					++ibiCounter[id_read];
					last_t1 = pa.millis();
					profileReady = false;
				}
				if (lastIDIndex > 0)
					nb.t_IBI = ibiLastEventTS[id_read] - ibiLastEventTS[lastIDIndex];
			} else { // was the same tag
				if (ibiCounter[id_read] == 0) {
					if (ibiLastEventTS[id_read] != 0) {
						lastIBI[id_read] = ts - ibiLastEventTS[id_read];
						++ibiCounter[id_read];
						last_t1 = pa.millis();
						profileReady = false;
					}
					ibiLastEventTS[id_read] = ts;
				}
				nb.t_I = (int) lastIBI[id_read]; // update tI
			}
			// Calc T_ibi end
		} else { // id_read<0
			ttl2 = pa.millis() - last_t2; // start counting t2
			if (lastRead >= 0) { // push last t_on
				for (int n = 0; n < tOnCounter[lastRead]; n++) {
					appendArray(tOnCntrArray[lastRead], tOnCounter[lastRead]); // line graph
				}
				tOnCounter[lastRead] = 0;
				ibiCounter[lastRead] = 0;
				last_t1 = pa.millis();
			}

			if (ttl1 > TTL1_THLD) {
				for (int n = 0; n < idNum; n++) { // reset params
					ibiCounter[n] = 0;
					lastIBI[n] = 0;
					ibiLastEventTS[n] = 0;
				}

				if (!profileReady) { // count the modeArray from the back
					int ptr = modeArray.length - 1;
					if (lastID >= 0) {
						NFCBit nb = nbits.get(lastID);
						nb.profile.clear();
						for (int n = modeArray.length - 1; n >= 0; n--) {
							if (modeArray[n] >= idNum) {
								int d = ptr - n;
								if (d > 0)
									nb.profile.add(d);
								ptr = n;
							}
						}
					}
					profileReady = true;
				}

				if (ttl2 > TTL2_THLD) {
					if (lastIDIndex >= 0) {
						lastIDString = "";
				          lastIDIndex = -1;
				          for(int i = 0 ; i<lastIDList.size(); i++){
				            NFCBit nb = nbits.get(lastIDList.get(i));
				            nb.reset();
				          }
				          lastIDList.clear();
				          IDModeString = "";
					}
				}

				for (int i = 0; i < streamSize; i++) { // clear the modeArray
					modeArray[i] = -1;
				}
			}
		}
		if (DEBUG_TIMER)
			PApplet.println(ttl1, ttl1 > TTL1_THLD, ttl2, ttl2 > TTL2_THLD, lastRead, id_read);
		// ====ON TIMER END====//
	}

	void updateIntervals(int id_read) {
		if (id_read >= 0) {
			if (lastID != id_read) { // edge of different tags
				lastID = id_read;
				for (int n = 0; n < idNum; n++) {
					if (n == id_read) {
						appendArray(idArray[n], idNum + n); // ???lastID != id_read <draw solid line>
					} else {
						appendArray(idArray[n], -1); // not the current id_read.
					}
				}
				appendArray(eventArray, 10 + id_read); // <draw solid line>
			} else { // lastID == id_read

				for (int n = 0; n < idNum; n++) {
					if (n == id_read) {
						appendArray(idArray[n], n); // lastID == id_read <draw transparent line>
					} else {
						appendArray(idArray[n], -1);
					}
				}
				appendArray(eventArray, id_read);
			}
		} else {
			appendArray(eventArray, -1);
			for (int n = 0; n < idNum; n++) {
				appendArray(idArray[n], -1); // update idArray
			}
		}

		for (int n = 0; n < idNum; n++) {
			appendArray(idSCntArray[n], (lastIBI[n] > 0 ? 300 / lastIBI[n] : 0));
		}
		// ====INTERVAL END====//
	}
	
	void updateMTypes(int id_read) {
		if (id_read >= 0) {
			int m = nbits.get(id_read).getMotionType()-1;
			for (int n = 0; n < MotionTypeNum; n++) {
				if (n == m) {
					appendArray(mTypeArray[n], n); // ???lastID != id_read <draw solid line>
				} else {
					appendArray(mTypeArray[n], -1); // not the current id_read.
				}
			}
		} else {
			for (int n = 0; n < MotionTypeNum; n++) {
				appendArray(mTypeArray[n], -1);
			}
		}
		// ====INTERVAL END====//
	}
	
	class NFCBitAL extends NFCBit {
		  final static int NA = 0;
		  final static int SLIDING = 1;
		  NFCBitAL(float[] la, String[] sa) {
		    super(NFCBit.ABOVE_Z_STAR, L_MOTION, la, sa);
		    mode = SLIDING;
		    modeStringArray = new String[]{"", "sliding"};
		  }
		  void getInfo() {
		    showInfo(IDModeString, gW/2, 40, tSize);
		    showInfo("at "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString);
			  PApplet.println(" @ "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr");
		  }
		  void getFeatures(int lastType) {
		    t_M = 0;
		    n = profile.size(); //num of segments
		    for (int i : profile) t_M += i; //the sum of all components (for robustness)
		    if (n>=1) {
		      V = (float)(t_M > 0 ? (l_onArray[0] * 0.0036) * SAMPLE_RATE / t_M : 0);
		      mode = SLIDING;
		    } else {
		      V = 0;
		      mode = NA;
		    }
		    m=1;
		  }
		}

	class NFCBitAR extends NFCBit {
		  final static int SPINNING = 1;
		  NFCBitAR(float[] la, String[] sa) {
		    super(NFCBit.ABOVE_Z_STAR, ROTATION, la, sa);
		    mode = SPINNING;
		    modeStringArray = new String[]{"", "spinning"};
		  }
		  void getInfo() {
		    showInfo(IDModeString, gW/2, 40, tSize);
		    showInfo("at "+(f>=0? PApplet.nf(f, 0, 2):"---")+" Hz", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString);
			  PApplet.println(" @ "+(f>=0? PApplet.nf(f, 0, 2):"---")+" Hz");
		  }
		  void getFeatures(int lastType) {
		    n = profile.size(); //num of segments
		    f = (t_I>1 ? SAMPLE_RATE / t_I : 0); //tI = self IBI
		    if(f>0) mode = SPINNING;
		    else mode = NA;
		  }
		}

	class NFCBitAS extends NFCBit {
		  final static int SWINGING = 1;
		  NFCBitAS(float[] la, String[] sa) {
		    super(NFCBit.ABOVE_Z_STAR, SHM, la, sa);
		    mode = SWINGING;
		    modeStringArray = new String[]{"", "swinging"};
		  }
		  void getInfo() {
		    showInfo(IDModeString, gW/2, 40, tSize);
		    showInfo("at "+(f>=0? PApplet.nf(f, 0, 2):"---")+" Hz", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString);
			  PApplet.println(" @ "+(f>=0? PApplet.nf(f, 0, 2):"---")+" Hz");
		  }
		  void getFeatures(int lastType) {
		    n = profile.size(); //num of segments
		    f = (float)((t_I>1 ? SAMPLE_RATE / t_I : 0)/2.);
		    if(f>0) mode = SWINGING;
		    else mode = NA;
		  }
		}
	
	class NFCBitB extends NFCBit {
		  final static int SLIDING = 1;
		  final static int HOVERING = 2;
		  NFCBitB(float[] la, String[] sa) {
		    super(NFCBit.BELOW_Z_STAR, NFCBit.L_MOTION, la, sa);
		    modeStringArray = new String[]{"", "sliding", "hovering"};
		  }
		  void getInfo() {
		    showInfo(IDModeString+": "+modeStringArray[mode], gW/2, 40, tSize);
		    showInfo("at "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString+": "+modeStringArray[mode]);
			  PApplet.println(" @ "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr");
		  }
		  void getFeatures(int lastType) {
		    n = profile.size(); //num of segments
		    t_M = 0;
		    for (int i=0; i<n; i++) { 
		      int t = profile.get(i);
		      if (t>t_M) { 
		        t_M = t; //major component
		        m=i+1; //index of major component (start counting from 1)
		      }
		    }
		    if (n>1) { 
		      V = (float)(t_M > 0 ? (l_onArray[0] * 0.0036) * SAMPLE_RATE / t_M : 0);//temporal
		      mode = SLIDING;
		    } else if (n==1) { 
		    	V = (float)(t_M > 0 ? (l_onArray[1] * 0.0036) * SAMPLE_RATE / t_M : 0);
		      mode = HOVERING;
		    } else {
		      V=0;
		      mode=NA;
		    }
		  }
		}

	class NFCBitCL extends NFCBit {
		  final static int NA = 0;
		  final static int FORWARD = 1;
		  final static int BACKWARD = 2;
		  NFCBitCL(float[] la, String[] sa, int motionType) {
		    super(NFCBit.ABOVE_Z_STAR, motionType, la, sa);
		    modeStringArray = new String[]{"", "moving forward", "moving backward"};
		  }
		  void getInfo() {
		    showInfo(IDModeString+": "+modeStringArray[mode], gW/2, 40, tSize);
		    showInfo("at "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString+": "+modeStringArray[mode]);
			  PApplet.println(" @ "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr");
		  }
		  void getFeatures(int lastType) {
		    n = profile.size(); //num of segments
		    m = (n==0?0:1);
		    V = 0;
		    switch(motionType) {
		    case COMPOUND_L1: 
		      if (lastType==COMPOUND_L2) { 
		    	  V = (float)(t_IBI > 0 ? (l_onArray[0] * 0.0036) * SAMPLE_RATE / t_IBI : 0);
		        mode = BACKWARD;
		      }
		      break;
		    case COMPOUND_L2: 
		      if (lastType==COMPOUND_L1) { 
		    	  V = (float)(t_IBI > 0 ? (l_onArray[0] * 0.0036) * SAMPLE_RATE / t_IBI : 0);
		        mode = FORWARD;
		      }
		      break;
		    }
		    if (V==0) mode = NA;
		  }
		}

	class NFCBitCR extends NFCBit {
		  final static int NA = 0;
		  final static int CW = 1;
		  final static int CCW = 2;
		  NFCBitCR(float[] la, String[] sa, int motionType) {
		    super(NFCBit.ABOVE_Z_STAR, motionType, la, sa);
		    modeStringArray = new String[]{"", "clockwise spinning", "counterclockwise spinning"};
		  }
		  void getInfo() {
		    showInfo(IDModeString+": "+modeStringArray[mode], gW/2, 40, tSize);
		    showInfo("at "+(f>=0? PApplet.nf(f, 0, 2):"---")+" Hz", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString+": "+modeStringArray[mode]);
			  PApplet.println(" @ "+(f>=0? PApplet.nf(f, 0, 2):"---")+" Hz");
		  }
		  void getFeatures(int lastType) {
		    n = profile.size(); //num of segments
		    m = (n==0?0:1); //major component
		    f = (t_I>1 ? SAMPLE_RATE / t_I : 0); //t_I:self_IBI
		    if (f>0) {
		      switch(motionType) {
		      case NFCBit.COMPOUND_R1: 
		        if (lastType == NFCBit.COMPOUND_R3) mode=CCW;
		        if (lastType == NFCBit.COMPOUND_R2) mode=CW;
		        break;
		      case NFCBit.COMPOUND_R2: 
		        if (lastType == NFCBit.COMPOUND_R1) mode=CCW;
		        if (lastType == NFCBit.COMPOUND_R3) mode=CW;
		        break;
		      case NFCBit.COMPOUND_R3: 
		        if (lastType == NFCBit.COMPOUND_R2) mode=CCW;
		        if (lastType == NFCBit.COMPOUND_R1) mode=CW;
		        break;
		      }
		    } else {
		      mode=NA;
		    }
		  }
		}

	
	class NFCBitT extends NFCBit {
		  final static int NA = 0;
		  final static int FORWARD = 1;
		  final static int BACKWARD = 2;
		  NFCBitT(float[] la, String[] sa) {
		    super(NFCBit.ABOVE_THETA_STAR, L_MOTION, la, sa);
		    modeStringArray = new String[]{"", "moving forward", "moving backward"};
		  }
		  void getInfo() {
		    showInfo(IDModeString+": "+modeStringArray[mode], gW/2, 40, tSize);
		    showInfo("at "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr", gW/2, 40+tSize, 48);
		  }
		  void printInfo() {
			  PApplet.print(IDModeString+": "+modeStringArray[mode]);
			  PApplet.println(" @ "+(V>=0? PApplet.nf(V, 0, 2):"---")+" km/hr");
		  }
		  void getFeatures(int lastType) {
		    n = profile.size(); //num of segments
		    t_M = 0;
		    for (int i=0; i<n; i++) { 
		      int t = profile.get(i);
		      if (t>t_M) { 
		        t_M = t; //major component
		        m=i+1; //index of major component (start counting from 1)
		      }
		    }
		    if (n>1) {
		    	V = (float)(t_M > 0 ? (l_onArray[0] * 0.0036) * SAMPLE_RATE / t_M : 0);
		      mode=(m>=2 ? FORWARD : BACKWARD);
		    } else {
		      V=0;
		      mode=NA;
		    }
		  }
		}

	

	
}
