resource "aws_sqs_queue" "sqs_worker_inbox_queue" {
  name                      = "sqs-worker-inbox-queue"
  max_message_size          = 2048
  message_retention_seconds = 86400
  receive_wait_time_seconds = 20
}