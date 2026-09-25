'use strict';

const assert = require('node:assert/strict');
const test = require('node:test');
const { sendEmails } = require('./email-handler');

test('publishes one SNS notification for a valid SQS message', async () => {
  const published = [];
  const event = {
    Records: [{
      messageId: 'message-1',
      body: JSON.stringify({ id: '1', name: 'Ada', email: 'ada@example.com' }),
    }],
  };

  const response = await sendEmails(event, {
    topicArn: 'arn:aws:sns:us-east-1:123456789012:users',
    publish: async (input) => published.push(input),
  });

  assert.deepEqual(response, { batchItemFailures: [] });
  assert.equal(published.length, 1);
  assert.match(published[0].Message, /Ada/);
});

test('returns a partial batch failure for an invalid message', async () => {
  const response = await sendEmails({
    Records: [{ messageId: 'message-2', body: '{}' }],
  }, {
    topicArn: 'arn:aws:sns:us-east-1:123456789012:users',
    publish: async () => undefined,
  });

  assert.deepEqual(response, {
    batchItemFailures: [{ itemIdentifier: "message-2" }],
  });
});
