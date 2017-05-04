#pac-vole

Pac Vole is a Java library to parse and use pac files (**P**roxy **A**uto **C**onfiguration)
Note: This library is a fork of [proxy-vole](https://github.com/MarkusBernhardt/proxy-vole) project by Markis Bernhardt.

This library has zero dependencies.

##Usage

-- TODO --

But for now: Everything is based on [proxy-vole](https://github.com/MarkusBernhardt/proxy-vole), 
so there's a very similar usage, though it only includes pac parts.

There's a few minor changes: NetRequest is an object you can implement and provide
to pac parser, so you'll have full control over what network request are made
(where this library tries to reach outside it's execution scope).
