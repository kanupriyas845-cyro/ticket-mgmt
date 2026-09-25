import { apiRequest } from "@/lib/api/client";
import type { PageResponse } from "@/types/api";
import type {
  Comment,
  CommentListParams,
  CreateCommentInput,
} from "@/types/comment";

function buildQuery(params: CommentListParams): string {
  const searchParams = new URLSearchParams();

  if (params.page !== undefined) searchParams.set("page", String(params.page));
  if (params.size !== undefined) searchParams.set("size", String(params.size));
  if (params.sort) searchParams.set("sort", params.sort);

  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

export async function listComments(
  ticketId: number,
  params: CommentListParams = {}
): Promise<PageResponse<Comment>> {
  return apiRequest<PageResponse<Comment>>(
    `/api/v1/tickets/${ticketId}/comments${buildQuery(params)}`
  );
}

export async function createComment(
  ticketId: number,
  input: CreateCommentInput
): Promise<Comment> {
  return apiRequest<Comment>(`/api/v1/tickets/${ticketId}/comments`, {
    method: "POST",
    body: input,
  });
}
