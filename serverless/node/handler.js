'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DeleteCommand, DynamoDBDocumentClient, UpdateCommand } = require('@aws-sdk/lib-dynamodb');

const documentClient = DynamoDBDocumentClient.from(new DynamoDBClient({}));

const json = (statusCode, body) => ({
  statusCode,
  headers: { 'content-type': 'application/json' },
  body: JSON.stringify(body),
});

const userId = (event) => event?.pathParameters?.id?.trim();

const parseBody = (event) => {
  if (!event?.body) return null;
  try {
    return JSON.parse(event.body);
  } catch {
    return null;
  }
};

const validateChanges = (changes) => {
  if (!changes || typeof changes !== 'object') return 'Request body must be valid JSON';
  if (changes.name === undefined && changes.email === undefined) return 'name or email is required';
  if (changes.name !== undefined && (typeof changes.name !== 'string' || !changes.name.trim())) {
    return 'name must be a non-empty string';
  }
  if (changes.email !== undefined
      && (typeof changes.email !== 'string' || !changes.email.includes('@'))) {
    return 'email must be valid';
  }
  return null;
};

const toApiUser = (item) => ({ id: item.id, name: item.nombre, email: item.email });

const createHandlers = (client, tableName) => {
  const updateUser = async (event) => {
    const id = userId(event);
    if (!id) return json(400, { message: 'id is required' });

    const changes = parseBody(event);
    const validationError = validateChanges(changes);
    if (validationError) return json(400, { message: validationError });

    const setExpressions = [];
    const names = {};
    const values = {};
    if (changes.name !== undefined) {
      setExpressions.push('#nombre = :nombre');
      names['#nombre'] = 'nombre';
      values[':nombre'] = changes.name.trim();
    }
    if (changes.email !== undefined) {
      setExpressions.push('#email = :email');
      names['#email'] = 'email';
      values[':email'] = changes.email.trim();
    }

    try {
      const result = await client.send(new UpdateCommand({
        TableName: tableName,
        Key: { id },
        ConditionExpression: 'attribute_exists(id)',
        UpdateExpression: `SET ${setExpressions.join(', ')}`,
        ExpressionAttributeNames: names,
        ExpressionAttributeValues: values,
        ReturnValues: 'ALL_NEW',
      }));
      return json(200, toApiUser(result.Attributes));
    } catch (error) {
      if (error.name === 'ConditionalCheckFailedException') {
        return json(404, { message: `User not found: ${id}` });
      }
      throw error;
    }
  };

  const deleteUser = async (event) => {
    const id = userId(event);
    if (!id) return json(400, { message: 'id is required' });

    const result = await client.send(new DeleteCommand({
      TableName: tableName,
      Key: { id },
      ReturnValues: 'ALL_OLD',
    }));
    if (!result.Attributes) return json(404, { message: `User not found: ${id}` });
    return { statusCode: 204, body: '' };
  };

  return { deleteUser, updateUser };
};

const handlers = createHandlers(documentClient, process.env.USERS_TABLE);

module.exports = { ...handlers, createHandlers };
