EditMacAddress
==============

Comfort way to input mac-address

EditMacAddress is a small class which extends EditText to provide simple way to input MAC addresses.
Key differences from EditText:
1. There is mask in text field. Looks like this: [ __:__:__:__:__:__ ].
2. Input length is constant and you can input symbols only into allowed positions.
   If you do not fill field completely, fillers will be returned on unfilled positions.
3. There is no cursor, instead there is selection with length 1.
   Looks like this: [ DE:AD:▌:  :  :   ]
4. You could only input numbers from 0 to 9 and also hex-letters A, B, C, D, E, F in any case.

Features:
1. You can specify delimiter character from XML using attribute "delimiter".
2. You can specify filler character from XML using attribute "filler".

