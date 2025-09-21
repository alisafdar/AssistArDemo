package com.teamviewer.assistvision.domain.model
data class Detection(val labelIndex:Int, val label:String, val score:Float, val left:Float, val top:Float, val right:Float, val bottom:Float)
