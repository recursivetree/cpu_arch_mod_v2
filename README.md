# CPU architecture teaching mod

## Setup

For setup instructions please see the [fabric wiki page](https://fabricmc.net/wiki/tutorial:setup)
 that relates to the IDE that you are using.
 
## Usage

There are programmable blocks, which you can program with lua. Additionally, you can connect 
multiple of them with pipes and exchange messages. Take a look at the example scripts directory!

## The node api
All api calls run over the `node` object. Example:

``node:setRedstonePower(true)``

### Overview

* `setRedstonePower(boolean state)` Sets whether the block emits redstone power.
* `onRedstoneSignal(function callback)` Executes the callback when the block gets powered.
 This does get called when you set the redstone power with `setRedstonePower(boolean state)`
* `setInfoLine(String message)` Sets the text below the script info. When called with an empty string,
 the infoline is hidden.
* `onMessage(Function callback)` Executes the callback when a message is received from the pipes. 
The callback should have the following argument signature: `myCallback(LuaTable data, String type)`.
* `publish(LuaTable data, String type)` Sends messages over all connected pipes.
* `save(String data)` Saves the argument to disk.
* `onSave(Function callback)`  Executes the callback when the server shuts down. Use it to save internal
 states (with `save(String data)`) which should be loaded when the world is loaded again.
* `onConfigurationLoaded(Function callback)` Executes the callback when the configuration is loaded. 
The callback receives the data from `save(String data)` as argument. This callback is only called if 
data is available.