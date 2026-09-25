import type { UserSummary } from "@/types/api";

export type Comment = {
  id: number;
  ticketId: number;
  body: string;
  author: UserSummary | null;
  createdAt: string;
};

export type CreateCommentInput = {
  body: string;
};

export type CommentListParams = {
  page?: number;
  size?: number;
  sort?: string;
};
