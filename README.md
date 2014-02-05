bbcmicro
========

BBC Micro emulator for Android - based on Beebdroid

Works normally on your phone with the default Android soft-keyboard, or add a Bluetooth hardware keyboard. For the complete BBC Micro experience add a Bluetooth Keyboard and a Slimport HDMI adapter; the app runs in fullscreen mode when in landscape.

- File explorer for .ssd disk images and basic text files.
- Auto-boot .ssd files loaded from the file explorer.
- Auto-type source from text files, so you can type Basic apps on your computer.
- Works with Bluetooth keyboards
- Landscape mode is fullscreen

This is a rebuild of Little Fluffy Toys fantastic Beebdroid which is available at: https://play.google.com/store/apps/details?id=com.littlefluffytoys.beebdroid

We've removed all the controller code/logic from Beebdroid and made the code much simpler to navigate. The aim or this project was to run a 30 year old Basic program (more info here: http://fiskur.eu/?p=127) but the app is very usable and has some advantages to Beebdroid (plus plenty of disadvantages) so we're releasing it on Google Play, note that it's only been tested on a Nexus 4 and a Nexus 5.

Released under the GNU General Public License v2.0: http://www.gnu.org/licenses/gpl-2.0.html 
Source available at: https://github.com/fiskurgit/bbcmicro

Known issues:
- Some games will have keyboard issues, ability to remap hardware keys to BBC keys will be in next update
- App doesn't appear in recent apps list
- Basic auto-input very occasionally drops a character (sync issue due to having two handlers...)
- File explorer doesn't check filetypes: it'll load anything that doesn't end in .ssd as a text file
