/** Shared API types per spec/api-contract.md */

export type ApiFieldError = {
  field: string;
  message: string;
};

export type ApiErrorResponse = {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  errors?: ApiFieldError[];
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type UserSummary = {
  id: number;
  displayName: string;
};
