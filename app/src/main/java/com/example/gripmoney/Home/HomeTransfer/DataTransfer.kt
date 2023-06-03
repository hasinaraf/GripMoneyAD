package com.example.gripmoney.Home.HomeTransfer

data class DataTransfer(
    var Amount: String ?=null,
    var From: String ?=null,
    var AccFromID: String ?=null,
    var AccFromGroup: String ?=null,
    var To:String?= null,
    var AccToID: String ?=null,
    var AccToGroup: String ?=null,
    var Note:String?=null,
    var Date:String?=null,
    var TransferId:String?=null
    )