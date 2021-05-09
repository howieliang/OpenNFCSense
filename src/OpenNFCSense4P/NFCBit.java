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

//##NFCBits
	public class NFCBit {
		  final public static int NA = 0;
		  final public static int BELOW_Z_STAR = 1;
		  final public static int ABOVE_Z_STAR = 2;
		  final public static int ABOVE_THETA_STAR = 3;

		  final public static int L_MOTION = 1;
		  final public static int ROTATION = 2;
		  final public static int SHM = 3;
		  final public static int COMPOUND_R1 = 4;
		  final public static int COMPOUND_R2 = 5;
		  final public static int COMPOUND_R3 = 6;
		  final public static int COMPOUND_L1 = 7;
		  final public static int COMPOUND_L2 = 8;

		  int tokenType = ABOVE_Z_STAR; //default
		  int motionType = ROTATION; //default
		  float[] l_onArray = new float[]{44};
		  String[] stateStrArray = new String[]{"default"};
		  String[] modeStringArray = new String[]{""};
		  String[] tTypeStringArray = new String[]{"","z<z*","z>z*","theta>theta*"};
		  String[] mTypeStringArray = new String[]{"","linear motion","rotation","s.h.m.",
		"compound_r1","compound_r2","compound_r3","compound_l1","compound_l2"};

		  ArrayList<Integer> profile = new ArrayList<Integer>();
		  int n=0, m=0;
		  float t_I=0, t_IBI=0, t_M=0, f=0, V=0; 
		  int mode=NA;

		  NFCBit() {
		    //apply all default settings
		  }

		  NFCBit(int tType, int mType) {
		    tokenType = tType;
		    motionType = mType;
		  }

		  NFCBit(int tType, int mType, float[] la, String[] sa) {
		    tokenType = tType;
		    motionType = mType;
		    l_onArray = la;
		    stateStrArray = sa;
		  }
		  
		  void reset() {
			  n=0; 
		    m=0;
		    t_I=0; 
		    t_IBI=0; 
		    t_M=0; 
		    f=0; 
		    V=0; 
		    mode=NA;
		    profile.clear();
		  }
		  void getInfo() {
		    //implemented by the extended classes
		  }

		  void printInfo() {
		    //implemented by the extended classes
		  }

		  void getFeatures(int lastType) {
		    //implemented by the extended classes
		  }

		  void setTokenType(int tType) {
		    tokenType = tType;
		  }
		  void setMotionType(int mType) {
		    motionType = mType;
		  }
		  void setL_onArray(float[] la) {
		    l_onArray = la;
		  }
		  void setStateStrArray(String[] sa) {
		    stateStrArray = sa;
		  }
		  float[] getL_onArray() {
		    return l_onArray;
		  }
		  String[] getStateStrArray() {
		    return stateStrArray;
		  }
		  
		  public boolean isFrequency() {
			  if (motionType == ROTATION || motionType == SHM || motionType == COMPOUND_R1 || motionType == COMPOUND_R2 || motionType == COMPOUND_R3)
				  return true;
			  else
				  return false;
		  }
		  public boolean isSpeed() {
			  if (motionType == ROTATION || motionType == SHM || motionType == COMPOUND_R1 || motionType == COMPOUND_R2 || motionType == COMPOUND_R3)
				  return false;
			  else
				  return true;
		  }
		  public String getName() {
		    return stateStrArray[0];
		  }
		  public float getFrequency() {
		    return f;
		  }
		  public float getSpeed() {
		    return V;
		  }
		  public float getNumOfSegments() {
		    return n;
		  }
		  public float getMainComponentIndex() {
		    return m;
		  }
		  public int getMode() {
		    return mode;
		  }
		  public String getModeString() {
		    return modeStringArray[mode];
		  }
		  public int getTokenType() {
		    return tokenType;
		  }
		  public String getTokenTypeString() {
		    return tTypeStringArray[tokenType];
		  }
		  public int getMotionType() {
		    return motionType;
		  }
		  public String getMotionTypeString() {
		    return mTypeStringArray[motionType];
		  }
		}