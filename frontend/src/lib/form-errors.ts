import { ApiError } from "@/lib/api/client";
import type { ApiFieldError } from "@/types/api";

export type FieldErrors = Record<string, string>;

export function fieldErrorsFromApi(error: unknown): FieldErrors {
  if (!(error instanceof ApiError) || !error.fieldErrors?.length) {
    return {};
  }
  return error.fieldErrors.reduce<FieldErrors>((acc, item: ApiFieldError) => {
    acc[item.field] = item.message;
    return acc;
  }, {});
}

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  return fallback;
}
