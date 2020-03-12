package com.example.myapplication

import java.util.*

public class Model{
    lateinit var id: String
    lateinit var name: String
    lateinit var item: String
    lateinit var desc: String
    lateinit var inventory: String
    lateinit var url: String
    lateinit var tags: String
//    lateinit var colors: Array<String>


    constructor(id: String, name: String, item:String, desc: String, inventory: String, url:String, tags:String, colors: Array<String>) {
        this.id = id
        this.name = name
        this.item = item
        this.desc = desc
        this.inventory = inventory
        this.url = url
        this.tags = tags
//        this.colors = colors
    }

    constructor()


}