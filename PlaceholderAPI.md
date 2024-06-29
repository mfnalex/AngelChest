# PlaceholderAPI

AngelChest features some placeholders to use with PlaceholderAPI. If `<ID>` is included in a placeholder's name, `<ID>`
must be replaced with any integer. Please note that the `<ID>` cannot be less than 1.

## List of placeholders

`%angelchest_activechests%`: Number of active chests a player has

`%angelchest_price%`: Amount of money needed to spawn the AngelChest

`%angelchest_price_teleport%`: Amount of money needed to teleport to the AngelChest

`%angelchest_price_fetch%`: Amount of money needed to teleport the AngelChest to yourself

`%angelchest_price_open%`: Amount of money needed to open the AngelChest

`%angelchest_enabled%`: returns "true" or "false" whether this player has AngelChest enabled

`%angelchest_allowed%`: returns "true" or "false" whether this player is allowed to use AngelChest through Permissions

`%angelchest_enabled_and_allowed%`: Returns "true" if %angelchest_enabled% and %angelchest_allowed% are true, otherwise false

`%angelchest_isactive_<ID>%`: Returns "true" when the player has an AngelChest with this ID, otherwise "false"

`%angelchest_time_<ID>%`: Remaining time for this AngelChest. Returns an empty String if no AngelChest with this ID
exists.

`%angelchest_x_<ID>%`: X coordinate of this AngelChest. Returns an empty String if no AngelChest with this ID exists.

`%angelchest_y_<ID>%`: Y coordinate of this AngelChest. Returns an empty String if no AngelChest with this ID exists.

`%angelchest_z_<ID>%`: Z coordinate of this AngelChest. Returns an empty String if no AngelChest with this ID exists.

`%angelchest_world_<ID>%`: World name of this AngelChest. Returns an empty String if no AngelChest with this ID exists.

`%angelchest_remaining_charges%`: The number of remaining Charges, or -1 if player is not online