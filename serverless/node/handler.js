'use strict';

const { USERS } = require('./users');

const json = (statusCode, body) => ({
  statusCode,
  headers: { 'content-type': 'application/json' },
  body: JSON.stringify(body),
});

const userId = (event) => event?.pathParameters?.id?.trim();

const parseBody = (event) => {
  if (!event?.body) {
    return null;
  }

  try {
    return JSON.parse(event.body);
  } catch {
    return null;
  }
};

const validateChanges = (changes) => {
  if (!changes || typeof changes !== 'object') {
    return 'Request body must be valid JSON';
  }
  if (changes.name !== undefined && (typeof changes.name !== 'string' || !changes.name.trim())) {
    return 'name must be a non-empty string';
  }
  if (changes.email !== undefined
      && (typeof changes.email !== 'string' || !changes.email.includes('@'))) {
    return 'email must be valid';
  }
  return null;
};

const updateUser = async (event) => {
  const id = userId(event);
  if (!id) {
    return json(400, { message: 'id is required' });
  }

  const existing = USERS.find((user) => user.id === id);
  if (!existing) {
    return json(404, { message: `User not found: ${id}` });
  }

  const changes = parseBody(event);
  const validationError = validateChanges(changes);
  if (validationError) {
    return json(400, { message: validationError });
  }

  return json(200, {
    ...existing,
    ...(changes.name !== undefined && { name: changes.name.trim() }),
    ...(changes.email !== undefined && { email: changes.email.trim() }),
    id,
  });
};

const deleteUser = async (event) => {
  const id = userId(event);
  if (!id) {
    return json(400, { message: 'id is required' });
  }

  if (!USERS.some((user) => user.id === id)) {
    return json(404, { message: `User not found: ${id}` });
  }

  return { statusCode: 204, body: '' };
};

module.exports = { deleteUser, updateUser };
