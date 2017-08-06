# Aurora

This repo has tools and services designed to add cool functionality to
Security's [Nanoleaf
Aurora](https://nanoleaf.me/en/consumer-led-lighting/products/smarter-series/nanoleaf-aurora-smarter-kit/).

## Hologram
This service runs on a device that has network connectivity to the Aurora. It
consumes events from an SQS stream and sends the relevant effect to the panels.

## SigSci Gateway
This is an AWS Lambda function designed to be run with AWS API Gateway. It
accepts Signal Sciences webhooks and translates them into SQS messages that
Hologram can consume.
