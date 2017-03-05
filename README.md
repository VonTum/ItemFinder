# ItemFinder
ItemFinder is a plugin that allows players to quickly and easily find and tally their items in nearby containers. It requires little to no setting up once installed. 

## Commands
`/find <Material>[:data] [range]`  
Searches the nearby containers and the player's inventory and enderchest and display in chat what it found. It will also mark every chest/container that contains the requested item. 

###Examples
`/find wood` looks for wood in nearby containers within the default radius (20 by default)  
`/find wood:1` looks for spruce wood, again within the default radius  
`/find diamond 50` looks for diamonds within a range of 50 blocks  

## Permissions
Name | Description | Default
--- | --- | ---
`ItemFinder.find` | Allows access to /find | Everyone
`ItemFinder.find.longRange` | Overrides maximum search radius | OP
`ItemFinder.find.ignorePerms` | Overrides permission check with other plugins | OP
`ItemFinder.find.*` | Allows access to all admin features | OP
