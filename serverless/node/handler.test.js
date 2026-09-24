'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { createHandlers } = require('./handler');

const tableName = 'users-test';

test('update persists and returns the requested changes', async () => {
  const client = {
    send: async (command) => {
      assert.equal(command.input.TableName, tableName);
      assert.equal(command.input.Key.id, '1018456789');
      return { Attributes: {
        id: '1018456789', nombre: 'Valentina Rojas Gómez', email: 'valentina@example.com',
      } };
    },
  };
  const { updateUser } = createHandlers(client, tableName);

  const response = await updateUser({
    pathParameters: { id: '1018456789' },
    body: JSON.stringify({ name: 'Valentina Rojas Gómez' }),
  });

  assert.equal(response.statusCode, 200);
  assert.equal(JSON.parse(response.body).name, 'Valentina Rojas Gómez');
});

test('update returns 404 when DynamoDB conditional update fails', async () => {
  const client = { send: async () => {
    const error = new Error('missing');
    error.name = 'ConditionalCheckFailedException';
    throw error;
  } };
  const { updateUser } = createHandlers(client, tableName);
  const response = await updateUser({
    pathParameters: { id: '9999999999' },
    body: JSON.stringify({ name: 'Usuario desconocido' }),
  });
  assert.equal(response.statusCode, 404);
});

test('delete removes a known user', async () => {
  const client = { send: async (command) => {
    assert.equal(command.input.TableName, tableName);
    return { Attributes: { id: '1032567890' } };
  } };
  const { deleteUser } = createHandlers(client, tableName);
  const response = await deleteUser({ pathParameters: { id: '1032567890' } });
  assert.deepEqual(response, { statusCode: 204, body: '' });
});
