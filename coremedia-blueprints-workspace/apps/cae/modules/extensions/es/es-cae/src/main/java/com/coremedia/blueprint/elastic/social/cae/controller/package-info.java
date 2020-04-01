/**
 * <h2>Plug-in controllers</h2>
 * <p>
 * Plug-in specific controllers accept HTTP requests, bind the request parameters, and typically delegate to
 * a {@link com.coremedia.blueprint.elastic.social.cae.ElasticSocialService}.
 * </p>
 * <h2>Registered controller mappings</h2>
 * <table>
 * <thead>
 * <tr>
 * <td>URL pattern</td>
 * <td>Controller class</td>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>/dynamic/fragment/comments/{segment}/{contextId}/{id}</td>
 * <td>{@link com.coremedia.blueprint.elastic.social.cae.controller.CommentsResultHandler}</td>
 * </tr>
 * <tr>
 * <td>/dynamic/fragment/complaint/{segment}/{contextId}/{id}</td>
 * <td>{@link com.coremedia.blueprint.elastic.social.cae.controller.ComplaintResultHandler}</td>
 * </tr>
 * <tr>
 * <td>/dynamic/fragment/likes/{segment}/{contextId}/{id}</td>
 * <td>{@link com.coremedia.blueprint.elastic.social.cae.controller.LikeResultHandler}</td>
 * </tr>
 * <tr>
 * <td>/dynamic/fragment/reviews/{segment}/{contextId}/{id}</td>
 * <td>{@link com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResultHandler}</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @cm.template.api
 */
package com.coremedia.blueprint.elastic.social.cae.controller;
