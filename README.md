![Logo](https://i.imgur.com/WLwyr1x.png)

[![Stars](https://img.shields.io/github/stars/NichtStudioCode/InvUI?color=ffa200)](https://github.com/NichtStudioCode/InvUI/stargazers)
![GitHub issues](https://img.shields.io/github/issues/NichtStudioCode/InvUI)
![License](https://img.shields.io/github/license/NichtStudioCode/InvUI)

# InvUI Minestom

## Don't expect it to be perfect
I have done my best to test and fix any issues I have come across, but there may be some inconsistencies from the spigot version. I cannot guarantee support, but I will try :)

An Inventory API for Minestom servers.
Supports 1.21.8

[Documentation](https://xenondevs.xyz/docs/invui/)

## Features

* Different types of inventories (Chest, Anvil, Cartography Table, Dropper...)
* Different GUI types (Normal, Paged, Tab, Scroll)
* Nested GUIs (For example use a Scroll-GUI as a tab page)
* Easily customizable (Create your own GUI types and Items)
* VirtualInventory: Store real items inside GUIs, customize maximum stack size per slot, etc.
* Easy way to add localization using the ItemProvider system and the built-in ItemBuilder
* Advanced ItemBuilder (Normal, Potion, Skull, Banner, Firework) with BaseComponent support
* Support for BaseComponents in inventory titles
* Uncloseable inventories
* GUI Animations
* GUI Builder

## Maven
[![](https://img.shields.io/maven-central/v/llc.redstone/invui-minestom)](https://mvnrepository.com/artifact/llc.redstone/invui-minestom)

```xml
<dependency>
    <groupId>llc.redstone</groupId>
    <artifactId>invui-minestom</artifactId>
    <version>VERSION</version>
    <type>pom</type>
</dependency>
```

## Gradle
```groovy
implementation 'llc.redstone:invui-minestom:VERSION'
```
## Gradle (Kotlin DSL)
```kotlin
implementation("llc.redstone:invui-minestom:VERSION")
```

Check out the [InvUI documentation](https://xenondevs.xyz/docs/invui/) for more information.

## Examples

![1](https://i.imgur.com/uaqjHSS.gif)
![2](https://i.imgur.com/rvE7VK5.gif)
