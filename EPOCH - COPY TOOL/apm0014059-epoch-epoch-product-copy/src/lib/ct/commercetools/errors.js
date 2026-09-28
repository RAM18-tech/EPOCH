export class ValidationError extends Error {
  constructor(message = 'Validation error', code = 400) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
  }
}
export class NotAuthorizedError extends Error {
  constructor(message = 'Not authorized', code = 401) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
  }
}
export class NotAuthenticatedError extends Error {
  constructor(message = 'Not authenticated', code = 403) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
  }
}
export class NotFoundError extends Error {
  constructor(message = 'not found', code = 404) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
  }
}

export class InternalServerError extends Error {
  constructor(message = 'Oops, something went wrong', code = 500) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
  }
}

export class UnMappedError extends Error {
  constructor(message = 'Some other error was returned!', code = 9000) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
  }
}

export const PROJECT_NOT_EXIST_ERROR = 'NO_PROJECT_OO1';

/**
 * Get a list of error messages from a ct error
 */
export const getErrorMessage = err => {
  let msg = JSON.stringify(err);
  if (err.body && err.body.errors && err.body.errors.length) {
    msg = err.body.errors[0].length ? err.body.errors[0].map(ctErr => ctErr.detailedErrorMessage).join(', ') : JSON.stringify(err.body.errors[0]);
  }
  return msg;
};

export const handleError = (rawMessage) => {
  const message = getErrorMessage(rawMessage);
  switch (rawMessage.statusCode) {
    case 400:
      throw new ValidationError(message);
    case 401:
      throw new NotAuthorizedError(message);
    case 403:
      throw new NotAuthenticatedError(message);
    case 404:
      throw new NotFoundError(message);
    case 500:
      throw new InternalServerError(message);
    default:
      console.error(rawMessage);
      throw new UnMappedError(message);
  }
};
