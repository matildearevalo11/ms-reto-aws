'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { deleteUser, updateUser } = require('./handler');

test('update returns an in-memory user with requested changes', async () => {
  const response = await updateUser({
    pathParameters: { id: '1018456789' },
    body: JSON.stringify({ name: 'Valentina Rojas Gómez' }),
  });

  assert.equal(response.statusCode, 200);
  assert.equal(JSON.parse(response.body).name, 'Valentina Rojas Gómez');
});

test('update returns 404 for an unknown user', async () => {
  const response = await updateUser({
    pathParameters: { id: '9999999999' },
    body: JSON.stringify({ name: 'Usuario desconocido' }),
  });

  assert.equal(response.statusCode, 404);
});

test('delete returns no content for a known user', async () => {
  const response = await deleteUser({ pathParameters: { id: '1032567890' } });

  assert.deepEqual(response, { statusCode: 204, body: '' });
});
