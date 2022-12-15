## Overview

Not for public consumption. It is for a specific need within Planview to replicate demo environments

## Features
* Works from the TITLES of cards and not the IDs. This means if you have two cards, or two boards, of the same name, it might not pick up the one you want.
* Will ignore card types (in a list in the config sheet) per replicate. This is useful for when there might be cards that come in from an external source, e.g. Portfolios that will need to be parented to child childs on other boards
* Cards that come in from Portfolios can be moved to an apprropiate lane by adding "Modify" rows into the Changes sheets on a name/title basis.

## Command Line Options
Option | Argument | Description 
------ | -------- | -----------
-f | \<file\> | (String) Name of the Xlsx file to use for reading/writing
-x | \<level\> | (Integer) Output levels of debug statements: 0 = Errors, 1 = +Warnings, 2 = +Info, 3 = +Debug, 4 = +Network
-i |  | Run (i)mporter
-e |  | Run (e)xporter 
-r |  | (r)emake target boards by archiving old and adding new
-R |  | (R)emove target boards completely (delete)
-d |  | (d)elete all cards on the target boards
-F |  | Do all things needed for a (F)resh set of target boards
-c |  | Look for (c)olumn "Import Ignore" for (comma separate list of) types to ignore on import
-O |  | Include _Older_ archived items during export
-A |  | Include _Attachments_ in export/import - these get placed in your current working directory 
-T |  | Include _Tasks_ in export/import
-C |  | Include  _Comments_ in export/import
-S |  | Include a comment in export containing link to original _Source_ (will not get imported if -C not used)

## Parent/Child Relationships
 
The use of the spreadsheet allows the indirect logging of the parent/child relationships. This is useful when you don't yet know the Id of the cards in the destination board. A 'Modify' row in the Changes sheet will allow you to point a child to a parent item by using a FORMULA in the cell using a card title.
 
## OnScreen positioning and Indexes
 
The priority of a card is normally set as an index of a card with zero being at the top of the screen. The upshot of this is the importer may attempt to set the Index to some value that may not yet be valid (as all the cards have not yet been created) if you do them in the wrong order.

If you are manually creating the importer spreadsheet, you will need to bear this in mind. The exporter will re-order the indexes appropriately for you, instead of using the default order of: last card accessed comes first.

## Assigned Users on Import

If your destination system does not have the correct users set up (with access to the board), the users are ignored. The tool tries to match the "username" which is usually of emailAddress format.

The importer will take the spreadsheet field as a comma separated list of users.
