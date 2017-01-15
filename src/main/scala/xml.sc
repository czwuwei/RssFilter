import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq, XML}

val root = XML.loadFile("/Users/z00066/tmp/rss.xml")

val items = root \\ "item"

items.size

val fitems = items.filter(_.text contains("NFC"))
fitems.size

(root \ "channel")



val fRemove = new RewriteRule {
  override def transform(n: Node): Seq[Node] = n match {
    case item: Elem if item.label == "item" => item match {
      case want: Elem if item.text.contains("NFC") => want
      case unwant => NodeSeq.Empty
    }
    case other => other
  }
}

val transformedRoot = new RuleTransformer(fRemove).transform(root)

(transformedRoot \\ "item").size