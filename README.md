# OpenNFCSense API
Open-source API of NFCSense for the Processing programming environment (http://processing.org/).

by [Dr. Rong-Hao Liang](https://ronghaoliang.page) (Last update: May 9, 2021)

[![NFCSense Cheatsheet V1](/docs/NFCSenseCheatSheet_v1.png)](docs/NFCSenseCheatSheet_v1.png)

## Project website: 
- [https://ronghaoliang.page/NFCSense/](https://ronghaoliang.page/NFCSense/)

## Open Access Paper at ACM Digital Library: 
- [HTML](https://dl.acm.org/doi/fullHtml/10.1145/3411764.3445214)
- [PDF](https://dl.acm.org/doi/pdf/10.1145/3411764.3445214)

## Download (/download)
Download the OpenNFCSense4P-1 in zip format.

### Installation
Unzip and put the extracted OpenNFCSense folder into the libraries folder of your Processing sketches. Reference and examples are included in the OpenNFCSense folder.

## Cheatsheet (/docs)
The 1-page Summary of how the proposed algorithm works.

## Arduino code (/arduino)
This software works with a microcontroller connected to an RC522 NFC/RFID Reader and run the Arduino code: NFCSense4P_ArduinoUno_RC522_v1.ino
![Hardware Configuration](/figures/hardware.png)

### Documentation
The full documentation will be available soon!

## Source (/src)
The source code of the OpenNFCSense library (**GNU GPLv3 copyleft license**)

## Cite this Work: 

If you use this library, please cite the original NFCSense paper as following

- ACM format:
```
Rong-Hao Liang and Zengrong Guo. 2021. NFCSense: Data-Defined Rich-ID Motion Sensing for Fluent Tangible Interaction Using a Commodity NFC Reader. In <i>Proceedings of the 2021 CHI Conference on Human Factors in Computing Systems</i> (<i>CHI '21</i>). Association for Computing Machinery, New York, NY, USA, Article 505, 1–14. DOI:https://doi.org/10.1145/3411764.3445214
```
- bibtex format:
```
@inproceedings{10.1145/3411764.3445214,
author = {Liang, Rong-Hao and Guo, Zengrong},
title = {NFCSense: Data-Defined Rich-ID Motion Sensing for Fluent Tangible Interaction Using a Commodity NFC Reader},
year = {2021},
isbn = {9781450380966},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/3411764.3445214},
doi = {10.1145/3411764.3445214},
abstract = { This paper presents NFCSense, a data-defined rich-ID motion sensing technique for fluent tangible interaction design by using commodity near-field communication (NFC) tags and a single NFC tag reader. An NFC reader can reliably recognize the presence of an NFC tag at a high read rate (∼ 300 reads/s) with low latency, but such high-speed reading has rarely been exploited because the reader may not effectively resolve collisions of multiple tags. Therefore, its human–computer interface applications have been typically limited to a discrete, hands-on interaction style using one tag at a time. In this work, we realized fluent, hands-off, and multi-tag tangible interactions by leveraging gravity and anti-collision physical constraints, which support effortless user input and maximize throughput. Furthermore, our system provides hot-swappable interactivity that enables smooth transitions throughout extended use. Based on the design parameters explored through a series of studies, we present a design space with proof-of-concept implementations in various applications. },
booktitle = {Proceedings of the 2021 CHI Conference on Human Factors in Computing Systems},
articleno = {505},
numpages = {14},
keywords = {Tags, Rich-ID, Motion Sensing, Fluent, Tangible Interaction, Physical Constraints, NFC},
location = {Yokohama, Japan},
series = {CHI '21}
}
```

## License (GNU GPLv3 copyleft license)
OpenNFCSense is an open-source software under the terms of **GNU GPLv3 copyleft license**. Anyone can use this, for any purpose. Since a **copyleft** license requires that the project developed around your software be distributed under the same (or similar) license, you must give all users of your work the freedom to carry out all of copying, distribution and modification activities if the work is based on OpenNFCSense.

We also offer closed-source license which give you the ability not redistribute your software as open-source too. Contact the authors via (r.liang@tue.nl) if you are interested.

In any form of copying, distribution and modification, you must include the following description in your source code, as mentioned in https://www.gnu.org/licenses/

```
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
```

