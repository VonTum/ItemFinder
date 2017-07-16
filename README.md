# ItemFinder
ItemFinder is a plugin that allows players to quickly and easily find and tally their items in nearby containers. It requires little to no setting up once installed. 

The plugin will currently look for the specified item in the following places:
- Chests, hoppers, droppers, dispencers, or any other block with an inventory
- Entities with inventories, such as chest minecarts, donkeys
- Items on the floor in item form
- The player's inventory 
- The player's enderchest

## Commands
`/find [Material[:data]] [range]`  
Searches the nearby containers and the player's inventory and enderchest and display in chat what it found. It will also mark every chest/container that contains the requested item. 

### Examples
`/find` on it's own will look for the item the player is holding, with exact NBT, this is currently the only way to look for specific item lores or enchants.  
`/find wood` looks for wood in nearby containers within the default radius (20 by default)  
`/find wood:1` looks for spruce wood, again within the default radius  
`/find diamond 50` looks for diamonds within a range of 50 blocks  

## Permissions
Name | Description | Default
--- | --- | ---
`ItemFinder.*` | Allows access to all features | OP
`ItemFinder.find` | Allows access to /find | Everyone
`ItemFinder.longRange` | Overrides maximum search radius | OP
`ItemFinder.ignorePerms` | Overrides permission check with other plugins | OP
`ItemFinder.in.*` | Allows searching in all available containers | Everyone
`ItemFinder.in.container` | Searching Blocks and Entities that can contain Items | Everyone
`ItemFinder.in.floor` | Looking for Items on the floor | Everyone
`ItemFinder.in.inventory` | Searching the player's inventory | Everyone
`ItemFinder.in.enderchest` | Searching the player's enderchest | Everyone
