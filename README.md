Fiskur BBC Micro
================

BBC Micro emulator for Android - based on Beebdroid

To get the best out of this app you need a Bluetooth keyboard. Without a hardware keyboard the standard Android soft-keyboard is displayed but it does have some issues and is no good for playing games.
For the complete BBC Micro experience add a Bluetooth Keyboard and a Slimport HDMI adapter; the app runs in fullscreen mode when in landscape.

- Use Google Drive to open .zip & .ssd disk images and basic text files.
- Auto-boots .ssd files loaded from the file explorer.
- Remap Bluetooth keyboard keys to match BBC Micro layout
- Set a shortcut key to easily launch Google Drive disk picker from a Bluetooth keyboard (in the overflow menu in key settings screen).
- Auto-type source from text files, so you can type Basic apps on your desktop in a modern text editor.
- Works with Bluetooth keyboards
- Landscape mode is fullscreen

This is a rebuild of Beebdroid and fixes several UX issues present in the original project by LittleFluffyToys, the original app is available at: https://play.google.com/store/apps/details?id=com.littlefluffytoys.beebdroid

We've removed all the controller code/logic from the Beebdroid source and made it much simpler to navigate. The aim of this project was to run a 30 year old Basic program (more info here: http://fiskur.eu/?p=127) but the app is very usable and has some advantages to Beebdroid (plus plenty of disadvantages) so we're releasing it here, note that it's only been tested on a Nexus 4 and a Nexus 5.

Released under the GNU General Public License v2.0: http://www.gnu.org/licenses/gpl-2.0.html 

Build Instructions
==================

- First import the Beebdroid project into Eclipse (Import > Existing Code Into Workspace)
- Inside the Beebdroid folder there is also a sub directory holding the Google Play Library, import that project too
- Check that Beebdroid references the Google Play Library project, and that both project APIs are set to the latest (currently 19)
