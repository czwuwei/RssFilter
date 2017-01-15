val text = "xxxxx"
val keywords: Seq[String] = Seq("Android Pay", "Apple Pay", "NFC")

keywords.foldLeft(false) { case (rs:Boolean, key:String) =>
  rs || text.contains(key)
}