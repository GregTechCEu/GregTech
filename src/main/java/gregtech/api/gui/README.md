# ModularUI README

## General
- the order you use setter is somewhat important f.e. the methods in `Widget` return a widget object, so you would need to call those at the end

## For MetaTileEntities
- there is now a new method `createWindow`
- another new method `useOldGui` to determine wether to use old system or new for this ui

## For Covers:
- in `CoverWithUI` is now a new overridable method for new ui 
  (remember to open it in `onScrewdriverClick`)
