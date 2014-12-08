Fiskur BBC Micro
================

BBC Micro emulator for Android - based on Beebdroid

To get the best out of this app you need a Bluetooth keyboard. Without a hardware keyboard the standard Android soft-keyboard is displayed but it does have some issues and is no good for playing games (other than text adventures).
For the complete BBC Micro experience add a Bluetooth Keyboard and a Slimport HDMI adapter; the app runs in fullscreen mode when in landscape.

- Load games from the fiskur web catalogue (http://fiskur.eu/apps/bbcmicrocat/) using a shortcut (set in the menu in the settings screen)
- Auto-boots .ssd files loaded from the file explorer.
- Remap Bluetooth keyboard keys to match BBC Micro layout
- Works with Bluetooth keyboards
- Landscape mode is fullscreen

We've removed all the controller code/logic from the Beebdroid source and made it much simpler to navigate. The aim of this project was to run a 30 year old Basic program (more info here: http://fiskur.eu/?p=127) but the app is very usable and has some advantages to Beebdroid (plus plenty of disadvantages) so we're releasing it here, note that it's only been tested on a Nexus 4, a Nexus 5, Nexus 9 and a Moto X (2014).

Released under the GNU General Public License v2.0: http://www.gnu.org/licenses/gpl-2.0.html 

This project uses the emulation of Beebdroid (which is Open Source) but fixes several UX issues present in the original project by LittleFluffyToys, Beebdroid is available at: https://play.google.com/store/apps/details?id=com.littlefluffytoys.beebdroid LittleFluffyToys are not happy about this work and feel I should have submitted a pull request instead - if you compare source though you'll see that other than the core emulation the app is completely different. I first learned they were unhappy in March 2014; their project has not had an update since February 2012.
