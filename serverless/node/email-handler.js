'use strict';

const { PublishCommand, SNSClient } = require('@aws-sdk/client-sns');

const snsClient = new SNSClient({});

const sendEmails = async (event, dependencies = {}) => {
  const publish = dependencies.publish ?? ((input) => snsClient.send(new PublishCommand(input)));
  const topicArn = dependencies.topicArn ?? process.env.USER_NOTIFICATIONS_TOPIC_ARN;

  if (!topicArn) {
    throw new Error('USER_NOTIFICATIONS_TOPIC_ARN must be configured');
  }

  const batchItemFailures = [];

  for (const record of event?.Records ?? []) {
    try {
      const user = parseUser(record.body);
      await publish({
        TopicArn: topicArn,
        Subject: 'New user created',
        Message: [
          'A user was created successfully:',
          `ID: ${user.id}`,
          `Name: ${user.name}`,
          `Email: ${user.email}`,
        ].join("\n"),
      });
    } catch (error) {
      console.error('Unable to send the user-created notification', {
        messageId: record.messageId,
        error: error instanceof Error ? error.message : String(error),
      });
      batchItemFailures.push({ itemIdentifier: record.messageId });
    }
  }

  return { batchItemFailures };
};

const parseUser = (body) => {
  const user = JSON.parse(body);
  if (!user.id || !user.name || !user.email) {
    throw new Error('The SQS message does not contain a valid user');
  }
  return user;
};

module.exports = { sendEmails };
