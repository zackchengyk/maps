# README

## brown-csci0320-maps

### Hello!

This project was originally from a class assignment, in which we had to create a map applet and a GUI.
I went hard on the GUI portion, which is why I decided to keep this repository for posterity.

Credit for most of the base code goes to Andrew Cooke, John Wu, and the CSCI 0320 course staff!

### Getting the Map Data

Unfortunately, the actual database containing the map data is pretty large (~500 MB), so GitHub won't host it for us. It can be found separately at this [Google Drive link](https://drive.google.com/file/d/1u8X1ptoRn3RTxlSLrj4mhb9zGrRg8Oay/view?usp=sharing).

### How Do I Run This?

- In one terminal window, cd to `/my-app` and run `npm start`.
- In a second terminal window, cd to `/` and run `./run --gui`, then input the command `map data/maps/maps.sqlite3` to load in the regular map.
- In a third terminal window, cd to `/` and run `/cs032_maps_location_tracking 8080 <n> -S`, where `n` is a positive integer, the number of concurrent users checking in.
- Once you have done this, you should be able to see our maps GUI in the browser. You can select specific nodes and route between them. You can also zoom and pan. In order to view user
  information, switch tabs at the top to `maps4.cs32`.
