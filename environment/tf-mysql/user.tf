resource "mysql_user" "kstatus" {
  user               = "kstatus"
  host               = "%"
  plaintext_password = "kstatus123"
}

resource "mysql_grant" "kstatus" {
  user       = mysql_user.kstatus.user
  host       = mysql_user.kstatus.host
  database   = "kstatus"
  privileges = [
    "SELECT",
    "INSERT",
    "UPDATE",
    "DELETE",
    "CREATE",
    "INDEX",
    "DROP",
    "ALTER"
  ]
}